package com.thoughtindex.tab.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IndexTabRepository extends JpaRepository<IndexTab, Long> {

    List<IndexTab> findAllByOrderByPositionAsc();

    /**
     * 탭별 메모 개수를 쿼리 "한 번"에 가져온다.
     * 탭마다 count 쿼리를 날리면 탭 수만큼 쿼리가 나간다 (N+1 문제).
     */
    @Query("select n.tab.id as tabId, count(n) as noteCount from Note n group by n.tab.id")
    List<TabNoteCount> countNotesPerTab();

    @Query("select count(n) from Note n where n.tab.id = :tabId")
    long countNotesByTabId(Long tabId);

    /** 인터페이스 기반 프로젝션: select 결과를 이 모양으로 받는다 */
    interface TabNoteCount {
        Long getTabId();

        long getNoteCount();
    }
}
