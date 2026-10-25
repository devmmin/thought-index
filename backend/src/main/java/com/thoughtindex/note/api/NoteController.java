package com.thoughtindex.note.api;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thoughtindex.global.common.PageResponse;
import com.thoughtindex.global.security.CurrentMember;
import com.thoughtindex.note.api.dto.NoteRequest;
import com.thoughtindex.note.api.dto.NoteResponse;
import com.thoughtindex.note.application.NoteService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /** GET /api/notes?tabId=1&page=0&size=20 */
    @GetMapping
    public PageResponse<NoteResponse> findByTab(
            @CurrentMember Long memberId,
            @RequestParam Long tabId,
            @PageableDefault(size = 20) Pageable pageable) {
        return noteService.findByTab(memberId, tabId, pageable);
    }

    /** GET /api/notes/search?q=스프링 */
    @GetMapping("/search")
    public PageResponse<NoteResponse> search(
            @CurrentMember Long memberId,
            @RequestParam("q") @NotBlank String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return noteService.search(memberId, keyword, pageable);
    }

    @GetMapping("/{noteId}")
    public NoteResponse findById(@CurrentMember Long memberId, @PathVariable Long noteId) {
        return noteService.findById(memberId, noteId);
    }

    @PostMapping
    public ResponseEntity<NoteResponse> create(@CurrentMember Long memberId,
                                               @Valid @RequestBody NoteRequest request) {
        NoteResponse created = noteService.create(memberId, request);
        return ResponseEntity.created(URI.create("/api/notes/" + created.id())).body(created);
    }

    @PutMapping("/{noteId}")
    public NoteResponse update(@CurrentMember Long memberId, @PathVariable Long noteId,
                               @Valid @RequestBody NoteRequest request) {
        return noteService.update(memberId, noteId, request);
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> delete(@CurrentMember Long memberId, @PathVariable Long noteId) {
        noteService.delete(memberId, noteId);
        return ResponseEntity.noContent().build();
    }
}
