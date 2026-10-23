package com.thoughtindex.tab.api.dto;

import com.thoughtindex.tab.domain.IndexTab;
import com.thoughtindex.tab.domain.TabColor;

/**
 * 엔티티를 그대로 내보내지 않고 응답 전용 DTO 로 감싼다.
 * → 엔티티 필드를 바꿔도 API 가 같이 흔들리지 않는다.
 */
public record IndexTabResponse(
        Long id,
        String name,
        TabColor color,
        int position,
        long noteCount
) {
    public static IndexTabResponse of(IndexTab tab, long noteCount) {
        return new IndexTabResponse(tab.getId(), tab.getName(), tab.getColor(), tab.getPosition(), noteCount);
    }
}
