package com.thoughtindex.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 앱에서 일어날 수 있는 에러를 한곳에 모아둔다.
 * 프론트는 message 대신 code 로 분기하면 문구가 바뀌어도 안 깨진다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값을 다시 확인해 주세요."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없어요."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식이에요."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "잠시 후 다시 시도해 주세요."),

    // 인덱스 탭
    TAB_NOT_FOUND(HttpStatus.NOT_FOUND, "인덱스를 찾을 수 없어요."),
    TAB_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "인덱스는 최대 8개까지 붙일 수 있어요."),
    TAB_ORDER_MISMATCH(HttpStatus.BAD_REQUEST, "순서를 바꿀 인덱스 목록이 현재와 달라요."),

    // 메모
    NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "메모를 찾을 수 없어요.");

    private final HttpStatus status;
    private final String message;
}
