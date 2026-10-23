package com.thoughtindex.tab.api.dto;

import com.thoughtindex.tab.domain.TabColor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 탭 생성/수정 요청. 둘 다 같은 필드라 하나로 쓴다.
 * (필드가 달라지는 순간 Create / Update 로 쪼개면 된다)
 */
public record IndexTabRequest(
        @NotBlank(message = "인덱스 이름을 적어 주세요.")
        @Size(max = 20, message = "인덱스 이름은 20자까지예요.")
        String name,

        @NotNull(message = "색을 골라 주세요.")
        TabColor color
) {
}
