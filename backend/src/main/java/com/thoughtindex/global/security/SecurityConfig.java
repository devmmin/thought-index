package com.thoughtindex.global.security;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.thoughtindex.global.config.CorsProperties;
import com.thoughtindex.member.application.AuthService;

/**
 * 누가 어디에 들어올 수 있는지 정하는 곳.
 *
 * 방식: 세션 대신 JWT(출입증). 로그인하면 서버가 서명한 토큰을 주고, 화면은 요청마다
 * "Authorization: Bearer 토큰" 을 붙인다. 서버는 서명만 확인하면 되니 로그인 상태를 기억할 필요가 없다(stateless).
 * → 서버를 여러 대로 늘리거나 재시작해도 로그인이 풀리지 않는다.
 */
@Configuration
@EnableConfigurationProperties({AuthProperties.class, CorsProperties.class})
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            AuthService authService,
                                            AuthProperties authProperties,
                                            ObjectProvider<ClientRegistrationRepository> googleClients) throws Exception {
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();

        http
                // 쿠키로 인증하지 않으니(헤더에 토큰) CSRF 공격 대상이 아니다
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                // 세션은 구글 로그인 왕복(state 확인) 때만 잠깐 생긴다
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .headers(h -> h.frameOptions(f -> f.sameOrigin())) // 로컬 H2 콘솔용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/providers").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(rs -> rs
                        .jwt(jwt -> {})
                        .authenticationEntryPoint(entryPoint))
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint));

        // 구글 키(환경변수)가 있을 때만 구글 로그인을 켠다. 없으면 이메일 로그인만 동작
        if (googleClients.getIfAvailable() != null) {
            http.oauth2Login(o -> o
                    .successHandler(new GoogleLoginSuccessHandler(authService, authProperties))
                    .failureHandler((req, res, ex) ->
                            res.sendRedirect(authProperties.frontendUrl() + "/login?error=google")));
        }
        return http.build();
    }

    /** 비밀번호 해시. {bcrypt} 처럼 알고리즘 이름을 앞에 붙여 저장해서, 나중에 알고리즘을 바꿔도 옛 해시를 읽을 수 있다 */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    JwtEncoder jwtEncoder(AuthProperties props) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(props)));
    }

    @Bean
    JwtDecoder jwtDecoder(AuthProperties props) {
        return NimbusJwtDecoder.withSecretKey(secretKey(props)).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsProperties.allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    private SecretKey secretKey(AuthProperties props) {
        byte[] bytes = props.jwtSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.auth.jwt-secret 은 32바이트 이상이어야 합니다 (HS256)");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
