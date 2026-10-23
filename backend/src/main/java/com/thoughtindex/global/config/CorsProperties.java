package com.thoughtindex.global.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml 의 app.cors.* 를 타입 안전하게 바인딩한다.
 * @Value("${...}") 를 여기저기 흩뿌리는 것보다 설정이 한곳에 모인다.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
