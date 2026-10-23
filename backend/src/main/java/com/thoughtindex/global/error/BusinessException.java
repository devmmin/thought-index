package com.thoughtindex.global.error;

import lombok.Getter;

/**
 * "예상 가능한" 비즈니스 규칙 위반. (없는 메모 조회, 탭 개수 초과 등)
 * 서비스는 이것만 던지고, HTTP 응답으로 바꾸는 건 GlobalExceptionHandler 가 맡는다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
