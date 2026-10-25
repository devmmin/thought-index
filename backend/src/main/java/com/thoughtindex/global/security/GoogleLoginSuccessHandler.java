package com.thoughtindex.global.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.thoughtindex.member.application.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 구글 로그인이 끝나면 호출된다.
 * 구글이 알려준 사람 정보로 우리 회원을 찾거나 만들고, 우리 JWT 를 발급해 화면으로 돌려보낸다.
 *
 * 토큰을 쿼리(?token=)가 아니라 해시(#token=)에 싣는 이유: 해시는 서버로 전송되지 않아
 * 서버 로그나 Referer 헤더에 토큰이 남지 않는다.
 */
@RequiredArgsConstructor
public class GoogleLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final AuthProperties authProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OidcUser user = (OidcUser) authentication.getPrincipal();
        String token = authService.loginWithGoogle(user.getSubject(), user.getEmail(), user.getFullName());

        // 구글 로그인 왕복 동안만 쓴 세션은 바로 버린다 (이후로는 JWT 만 쓴다)
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(authProperties.frontendUrl() + "/auth/callback#token=" + token);
    }
}
