package com.thoughtindex.note.api.dto;

import java.time.LocalDateTime;

import com.thoughtindex.note.domain.Note;

public record NoteResponse(
        Long id,
        Long tabId,
        String title,
        String content,
        boolean pinned,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NoteResponse from(Note note) {
        return new NoteResponse(
                note.getId(),
                // 프록시의 id 만 꺼내는 건 추가 쿼리가 안 나간다
                note.getTab().getId(),
                note.getTitle(),
                note.getContent(),
                note.isPinned(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
