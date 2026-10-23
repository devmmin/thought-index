package com.thoughtindex.global.error;

import java.util.List;

/**
 * 모든 에러 응답의 모양을 통일한다.
 * { "code": "NOTE_NOT_FOUND", "message": "...", "fieldErrors": [] }
 */
public record ErrorResponse(
        String code,
        String message,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String reason) {
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage(), List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage(), fieldErrors);
    }
}
