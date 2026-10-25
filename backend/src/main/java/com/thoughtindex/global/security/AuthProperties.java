package com.thoughtindex.global.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * app.auth.* 설정.
 * jwtSecret 은 토큰 서명용 비밀 키라 운영에서는 반드시 환경변수(JWT_SECRET)로만 넣는다.
 */
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        String jwtSecret,
        Duration tokenTtl,
        String frontendUrl
) {
}
