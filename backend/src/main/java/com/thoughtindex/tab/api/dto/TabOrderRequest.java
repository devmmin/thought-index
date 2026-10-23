package com.thoughtindex.tab.api.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

/** 바뀐 순서대로 탭 id 를 전부 보낸다. 예: [3, 1, 2] */
public record TabOrderRequest(
        @NotEmpty List<Long> tabIds
) {
}
