package com.thoughtindex.global.common;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Spring 의 Page 를 그대로 JSON 으로 내보내면 내부 구현 필드가 섞여 나와 API 모양이 불안정하다.
 * 프론트가 필요한 것만 담은 고정된 응답 모양을 만든다.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        boolean hasNext
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.hasNext()
        );
    }
}
