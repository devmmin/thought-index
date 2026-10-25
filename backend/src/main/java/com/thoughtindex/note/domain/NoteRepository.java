package com.thoughtindex.note.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NoteRepository extends JpaRepository<Note, Long> {

    /** 고정한 메모 먼저, 그다음 최근 수정순 */
    Page<Note> findByTabIdOrderByPinnedDescUpdatedAtDesc(Long tabId, Pageable pageable);

    /**
     * 내 메모 중에서 제목 + 내용 검색. 메서드 이름이 너무 길어지는 쿼리는 @Query 로 직접 쓰는 게 읽기 쉽다.
     * join fetch: 결과마다 탭 이름을 쓰는데, 지연로딩이면 메모 수만큼 탭 조회 쿼리가 더 나간다 (N+1)
     */
    @Query(value = """
            select n from Note n join fetch n.tab t
            where t.memberId = :memberId
              and (lower(n.title) like lower(concat('%', :keyword, '%'))
                   or lower(n.content) like lower(concat('%', :keyword, '%')))
            order by n.updatedAt desc
            """,
            countQuery = """
            select count(n) from Note n join n.tab t
            where t.memberId = :memberId
              and (lower(n.title) like lower(concat('%', :keyword, '%'))
                   or lower(n.content) like lower(concat('%', :keyword, '%')))
            """)
    Page<Note> search(Long memberId, String keyword, Pageable pageable);

    /**
     * 탭 삭제 시 메모를 한 방에 지운다.
     * deleteAll(list) 는 한 건씩 DELETE 를 날리지만, 이건 쿼리 1번.
     *
     * 주의: 벌크 쿼리는 영속성 컨텍스트(1차 캐시)를 건너뛰고 DB 로 바로 간다.
     * 이미 메모리에 올라와 있던 Note 는 "DB 에선 지워졌는데 메모리엔 남은" 상태가 되므로
     *   flushAutomatically: 실행 전에 쌓인 변경을 먼저 DB 에 반영
     *   clearAutomatically: 실행 후 1차 캐시를 비워 낡은 엔티티를 버림
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Note n where n.tab.id = :tabId")
    int deleteAllByTabId(Long tabId);
}
