package com.thoughtindex.tab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.thoughtindex.global.error.BusinessException;
import com.thoughtindex.global.error.ErrorCode;
import com.thoughtindex.tab.api.dto.IndexTabRequest;
import com.thoughtindex.tab.application.IndexTabService;
import com.thoughtindex.tab.domain.IndexTab;
import com.thoughtindex.tab.domain.IndexTabRepository;
import com.thoughtindex.tab.domain.TabColor;

/**
 * 단위 테스트: 스프링도 DB 도 안 띄우고 서비스의 "규칙"만 빠르게 검증한다.
 * 리포지토리는 Mockito 가짜(mock)로 대신한다.
 */
@ExtendWith(MockitoExtension.class)
class IndexTabServiceTest {

    @Mock
    IndexTabRepository indexTabRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    IndexTabService indexTabService;

    static final Long ME = 1L;

    @Test
    @DisplayName("탭이 이미 8개면 더 붙일 수 없다")
    void create_overLimit() {
        given(indexTabRepository.countByMemberId(ME)).willReturn((long) IndexTab.MAX_COUNT);

        assertThatThrownBy(() -> indexTabService.create(ME, new IndexTabRequest("새 탭", TabColor.PINK)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TAB_LIMIT_EXCEEDED);
        verify(indexTabRepository, never()).save(any());
    }

    @Test
    @DisplayName("새 탭은 맨 아래(position = 현재 개수)에 붙고 이름 앞뒤 공백은 지운다")
    void create_appendsToBottom() {
        given(indexTabRepository.countByMemberId(ME)).willReturn(3L);
        given(indexTabRepository.save(any(IndexTab.class))).willAnswer(inv -> inv.getArgument(0));

        var response = indexTabService.create(ME, new IndexTabRequest("  공부  ", TabColor.SKY));

        assertThat(response.position()).isEqualTo(3);
        assertThat(response.name()).isEqualTo("공부");
    }

    @Test
    @DisplayName("순서 변경은 현재 탭 전체 id 를 보내야 한다")
    void reorder_mismatch() {
        given(indexTabRepository.findAllByMemberIdOrderByPositionAsc(ME)).willReturn(List.of(tab(1L, 0), tab(2L, 1)));

        assertThatThrownBy(() -> indexTabService.reorder(ME, List.of(1L)))
                .extracting("errorCode").isEqualTo(ErrorCode.TAB_ORDER_MISMATCH);
    }

    @Test
    @DisplayName("보낸 순서대로 position 이 다시 매겨진다")
    void reorder() {
        IndexTab first = tab(1L, 0);
        IndexTab second = tab(2L, 1);
        given(indexTabRepository.findAllByMemberIdOrderByPositionAsc(ME)).willReturn(List.of(first, second));

        indexTabService.reorder(ME, List.of(2L, 1L));

        assertThat(second.getPosition()).isZero();
        assertThat(first.getPosition()).isEqualTo(1);
    }

    private IndexTab tab(Long id, int position) {
        IndexTab tab = IndexTab.create(ME, "탭" + id, TabColor.MINT, position);
        // id 는 DB 가 만들어주는 값이라 단위 테스트에서만 강제로 넣는다
        ReflectionTestUtils.setField(tab, "id", id);
        return tab;
    }
}
