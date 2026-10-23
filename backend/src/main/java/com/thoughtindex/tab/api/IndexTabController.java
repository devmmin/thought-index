package com.thoughtindex.tab.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thoughtindex.tab.api.dto.IndexTabRequest;
import com.thoughtindex.tab.api.dto.IndexTabResponse;
import com.thoughtindex.tab.api.dto.TabOrderRequest;
import com.thoughtindex.tab.application.IndexTabService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 컨트롤러는 얇게: 요청 받기 → 검증(@Valid) → 서비스 호출 → 상태코드 정해서 응답. 로직은 넣지 않는다.
 */
@RestController
@RequestMapping("/api/tabs")
@RequiredArgsConstructor
public class IndexTabController {

    private final IndexTabService indexTabService;

    @GetMapping
    public List<IndexTabResponse> findAll() {
        return indexTabService.findAll();
    }

    @PostMapping
    public ResponseEntity<IndexTabResponse> create(@Valid @RequestBody IndexTabRequest request) {
        IndexTabResponse created = indexTabService.create(request);
        // 201 Created + 새로 생긴 자원의 위치
        return ResponseEntity.created(URI.create("/api/tabs/" + created.id())).body(created);
    }

    @PutMapping("/{tabId}")
    public IndexTabResponse update(@PathVariable Long tabId, @Valid @RequestBody IndexTabRequest request) {
        return indexTabService.update(tabId, request);
    }

    @PutMapping("/order")
    public ResponseEntity<Void> reorder(@Valid @RequestBody TabOrderRequest request) {
        indexTabService.reorder(request.tabIds());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tabId}")
    public ResponseEntity<Void> delete(@PathVariable Long tabId) {
        indexTabService.delete(tabId);
        return ResponseEntity.noContent().build();
    }
}
