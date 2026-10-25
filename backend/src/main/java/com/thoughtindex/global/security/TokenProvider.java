package com.thoughtindex.global.security;

import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 로그인에 성공한 회원에게 줄 출입증(JWT)을 만든다.
 * 출입증 안에는 "누구인지(sub = 회원 id)"와 "언제까지 유효한지"만 넣는다. 비밀번호 같은 건 절대 넣지 않는다.
 */
@Component
@RequiredArgsConstructor
public class TokenProvider {

    private final JwtEncoder jwtEncoder;
    private final AuthProperties authProperties;

    public String issue(Long memberId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("thought-index")
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiresAt(now.plus(authProperties.tokenTtl()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
