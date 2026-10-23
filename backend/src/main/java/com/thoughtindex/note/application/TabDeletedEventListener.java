package com.thoughtindex.note.application;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.thoughtindex.note.domain.NoteRepository;
import com.thoughtindex.tab.domain.TabDeletedEvent;

import lombok.RequiredArgsConstructor;

/**
 * 탭이 지워질 때 그 탭의 메모를 정리한다.
 *
 * @EventListener 는 이벤트를 발행한 쪽과 "같은 스레드, 같은 트랜잭션"에서 바로 실행된다.
 * 그래서 여기서 실패하면 탭 삭제도 같이 롤백된다 (메모만 남거나 탭만 남는 일이 없다).
 */
@Component
@RequiredArgsConstructor
public class TabDeletedEventListener {

    private final NoteRepository noteRepository;

    @EventListener
    public void handle(TabDeletedEvent event) {
        noteRepository.deleteAllByTabId(event.tabId());
    }
}
