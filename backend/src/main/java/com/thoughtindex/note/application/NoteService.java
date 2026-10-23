package com.thoughtindex.note.application;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thoughtindex.global.common.PageResponse;
import com.thoughtindex.global.error.BusinessException;
import com.thoughtindex.global.error.ErrorCode;
import com.thoughtindex.note.api.dto.NoteRequest;
import com.thoughtindex.note.api.dto.NoteResponse;
import com.thoughtindex.note.domain.Note;
import com.thoughtindex.note.domain.NoteRepository;
import com.thoughtindex.tab.application.IndexTabService;
import com.thoughtindex.tab.domain.IndexTab;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteService {

    private final NoteRepository noteRepository;
    // 다른 도메인의 리포지토리를 직접 쓰지 않고 그 도메인의 서비스를 거친다 → "탭이 없으면 404" 규칙을 재사용
    private final IndexTabService indexTabService;

    public PageResponse<NoteResponse> findByTab(Long tabId, Pageable pageable) {
        indexTabService.getTab(tabId); // 없는 탭이면 빈 목록 대신 404
        return PageResponse.from(
                noteRepository.findByTabIdOrderByPinnedDescUpdatedAtDesc(tabId, pageable).map(NoteResponse::from));
    }

    public PageResponse<NoteResponse> search(String keyword, Pageable pageable) {
        return PageResponse.from(noteRepository.search(keyword.strip(), pageable).map(NoteResponse::from));
    }

    public NoteResponse findById(Long noteId) {
        return NoteResponse.from(getNote(noteId));
    }

    @Transactional
    public NoteResponse create(NoteRequest request) {
        IndexTab tab = indexTabService.getTab(request.tabId());
        Note note = Note.write(tab, request.title().strip(), request.contentOrEmpty(), request.pinnedOrFalse());
        return NoteResponse.from(noteRepository.save(note));
    }

    @Transactional
    public NoteResponse update(Long noteId, NoteRequest request) {
        Note note = getNote(noteId);
        if (!note.getTab().getId().equals(request.tabId())) {
            note.moveTo(indexTabService.getTab(request.tabId()));
        }
        note.edit(request.title().strip(), request.contentOrEmpty(), request.pinnedOrFalse());
        // 응답의 updatedAt 이 방금 시각이 되도록 UPDATE 를 지금 반영
        noteRepository.flush();
        return NoteResponse.from(note);
    }

    @Transactional
    public void delete(Long noteId) {
        noteRepository.delete(getNote(noteId));
    }

    private Note getNote(Long noteId) {
        return noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTE_NOT_FOUND));
    }
}
