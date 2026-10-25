package com.thoughtindex.member.api;

import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.thoughtindex.global.security.CurrentMember;
import com.thoughtindex.member.api.dto.LoginRequest;
import com.thoughtindex.member.api.dto.MemberResponse;
import com.thoughtindex.member.api.dto.SignupRequest;
import com.thoughtindex.member.api.dto.TokenResponse;
import com.thoughtindex.member.application.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ObjectProvider<ClientRegistrationRepository> googleClients;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public MemberResponse me(@CurrentMember Long memberId) {
        return authService.me(memberId);
    }

    /** 화면이 구글 버튼을 보여줄지 정할 때 쓴다 (서버에 구글 키가 설정돼 있을 때만 true) */
    @GetMapping("/providers")
    public Map<String, Boolean> providers() {
        return Map.of("google", googleClients.getIfAvailable() != null);
    }
}
