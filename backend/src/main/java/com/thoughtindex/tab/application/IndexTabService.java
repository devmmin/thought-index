package com.thoughtindex.tab.application;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thoughtindex.global.error.BusinessException;
import com.thoughtindex.global.error.ErrorCode;
import com.thoughtindex.tab.api.dto.IndexTabRequest;
import com.thoughtindex.tab.api.dto.IndexTabResponse;
import com.thoughtindex.tab.domain.IndexTab;
import com.thoughtindex.tab.domain.IndexTabRepository;
import com.thoughtindex.tab.domain.IndexTabRepository.TabNoteCount;
import com.thoughtindex.tab.domain.TabDeletedEvent;

import lombok.RequiredArgsConstructor;

/**
 * 비즈니스 규칙은 여기에 둔다. (컨트롤러는 HTTP 만, 리포지토리는 DB 만)
 *
 * 클래스에 readOnly = true 를 기본으로 깔고, 쓰기 메서드에만 @Transactional 을 다시 붙인다.
 * → 조회 메서드에서 실수로 데이터를 바꾸는 걸 막고, 읽기 성능도 조금 이득.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexTabService {

    private final IndexTabRepository indexTabRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<IndexTabResponse> findAll() {
        Map<Long, Long> noteCounts = indexTabRepository.countNotesPerTab().stream()
                .collect(Collectors.toMap(TabNoteCount::getTabId, TabNoteCount::getNoteCount));

        return indexTabRepository.findAllByOrderByPositionAsc().stream()
                .map(tab -> IndexTabResponse.of(tab, noteCounts.getOrDefault(tab.getId(), 0L)))
                .toList();
    }

    @Transactional
    public IndexTabResponse create(IndexTabRequest request) {
        long count = indexTabRepository.count();
        if (count >= IndexTab.MAX_COUNT) {
            throw new BusinessException(ErrorCode.TAB_LIMIT_EXCEEDED);
        }
        // 새 탭은 맨 아래에 붙인다
        IndexTab tab = IndexTab.create(request.name().strip(), request.color(), (int) count);
        return IndexTabResponse.of(indexTabRepository.save(tab), 0);
    }

    @Transactional
    public IndexTabResponse update(Long tabId, IndexTabRequest request) {
        IndexTab tab = getTab(tabId);
        // save() 를 다시 부르지 않아도 된다: 트랜잭션이 끝날 때 바뀐 필드를 JPA 가 알아서 UPDATE (변경 감지)
        tab.update(request.name().strip(), request.color());
        return IndexTabResponse.of(tab, indexTabRepository.countNotesByTabId(tabId));
    }

    @Transactional
    public void reorder(List<Long> tabIds) {
        List<IndexTab> tabs = indexTabRepository.findAll();
        // 일부만 보내거나 없는 id 를 섞으면 순서가 꼬이니 "현재 탭 전체"와 정확히 같아야 한다
        boolean sameSet = tabs.size() == tabIds.size()
                && new HashSet<>(tabIds).equals(tabs.stream().map(IndexTab::getId).collect(Collectors.toSet()));
        if (!sameSet) {
            throw new BusinessException(ErrorCode.TAB_ORDER_MISMATCH);
        }

        Map<Long, IndexTab> byId = tabs.stream().collect(Collectors.toMap(IndexTab::getId, Function.identity()));
        for (int i = 0; i < tabIds.size(); i++) {
            byId.get(tabIds.get(i)).moveTo(i);
        }
    }

    @Transactional
    public void delete(Long tabId) {
        getTab(tabId); // 없으면 404
        // 리스너가 같은 트랜잭션 안에서 바로 메모를 지운다 → 그다음 탭 삭제 (FK 안 걸림)
        eventPublisher.publishEvent(new TabDeletedEvent(tabId));
        // 리스너의 벌크 삭제가 1차 캐시를 비웠으니, 위에서 꺼낸 엔티티 대신 id 로 다시 찾아 지운다
        indexTabRepository.deleteById(tabId);

        // 지운 탭 아래 있던 탭들을 한 칸씩 당겨 position 을 0..n-1 로 유지
        List<IndexTab> remaining = indexTabRepository.findAllByOrderByPositionAsc();
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).moveTo(i);
        }
    }

    /** 다른 도메인(note)에서도 "존재하는 탭"을 가져갈 때 쓴다 */
    public IndexTab getTab(Long tabId) {
        return indexTabRepository.findById(tabId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAB_NOT_FOUND));
    }
}
