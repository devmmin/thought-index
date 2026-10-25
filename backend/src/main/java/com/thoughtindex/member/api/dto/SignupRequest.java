package com.thoughtindex.member.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "이메일을 적어 주세요.")
        @Email(message = "이메일 형식이 아니에요.")
        @Size(max = 254)
        String email,

        // BCrypt 는 72바이트까지만 쓰므로 그 이상은 막는다
        @NotBlank(message = "비밀번호를 적어 주세요.")
        @Size(min = 8, max = 72, message = "비밀번호는 8~72자예요.")
        String password,

        @Size(max = 30, message = "이름은 30자까지예요.")
        String name
) {
}
