package com.thoughtindex.tab.domain;

/**
 * "탭이 지워진다"는 사실을 알리는 이벤트.
 *
 * 탭이 지워질 때 그 안의 메모도 지워야 하는데, tab 패키지가 note 패키지를 직접 부르면
 * tab ↔ note 가 서로를 아는 순환 의존이 생긴다. 이벤트로 알리기만 하고, 메모 정리는 note 쪽이 맡는다.
 */
public record TabDeletedEvent(Long tabId) {
}
