package com.thoughtindex.member.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "이메일을 적어 주세요.") String email,
        @NotBlank(message = "비밀번호를 적어 주세요.") String password
) {
}
