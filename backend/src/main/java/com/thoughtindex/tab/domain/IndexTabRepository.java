package com.thoughtindex.tab.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 모든 조회에 memberId 가 붙는다. "내 것만" 조건을 리포지토리 단계에서 걸어서
 * 서비스에서 실수로 남의 탭을 꺼내는 일이 없게 한다.
 */
public interface IndexTabRepository extends JpaRepository<IndexTab, Long> {

    List<IndexTab> findAllByMemberIdOrderByPositionAsc(Long memberId);

    Optional<IndexTab> findByIdAndMemberId(Long id, Long memberId);

    long countByMemberId(Long memberId);

    /**
     * 탭별 메모 개수를 쿼리 "한 번"에 가져온다.
     * 탭마다 count 쿼리를 날리면 탭 수만큼 쿼리가 나간다 (N+1 문제).
     */
    @Query("select n.tab.id as tabId, count(n) as noteCount from Note n where n.tab.memberId = :memberId group by n.tab.id")
    List<TabNoteCount> countNotesPerTab(Long memberId);

    @Query("select count(n) from Note n where n.tab.id = :tabId")
    long countNotesByTabId(Long tabId);

    /** 인터페이스 기반 프로젝션: select 결과를 이 모양으로 받는다 */
    interface TabNoteCount {
        Long getTabId();

        long getNoteCount();
    }
}
