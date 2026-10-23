package com.thoughtindex.note.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 메모 작성/수정 요청.
 * 수정은 PUT(전체 교체)이라 화면의 편집 내용을 통째로 보낸다.
 */
public record NoteRequest(
        @NotNull(message = "어느 인덱스에 꽂을지 골라 주세요.")
        Long tabId,

        @NotBlank(message = "제목을 적어 주세요.")
        @Size(max = 100, message = "제목은 100자까지예요.")
        String title,

        @Size(max = 10000, message = "내용은 10,000자까지예요.")
        String content,

        // primitive boolean 이면 Jackson 3 는 필드가 빠졌을 때 요청 자체를 거절한다 → 래퍼 타입 + 기본값 메서드
        Boolean pinned
) {
    /** content 를 안 보내면(null) 빈 문자열로 */
    public String contentOrEmpty() {
        return content == null ? "" : content;
    }

    /** pinned 를 안 보내면 false */
    public boolean pinnedOrFalse() {
        return Boolean.TRUE.equals(pinned);
    }
}
