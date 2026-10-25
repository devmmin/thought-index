package com.thoughtindex.member.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thoughtindex.global.error.BusinessException;
import com.thoughtindex.global.error.ErrorCode;
import com.thoughtindex.global.security.TokenProvider;
import com.thoughtindex.member.api.dto.LoginRequest;
import com.thoughtindex.member.api.dto.MemberResponse;
import com.thoughtindex.member.api.dto.SignupRequest;
import com.thoughtindex.member.api.dto.TokenResponse;
import com.thoughtindex.member.domain.Member;
import com.thoughtindex.member.domain.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        String email = normalize(request.email());
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_USED);
        }
        String name = (request.name() == null || request.name().isBlank())
                ? email.substring(0, email.indexOf('@'))
                : request.name().strip();
        Member member = memberRepository.save(
                Member.withPassword(email, passwordEncoder.encode(request.password()), name));
        return issue(member);
    }

    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(normalize(request.email()))
                // 이메일이 없는 건지 비밀번호가 틀린 건지 구분해서 알려주지 않는다 (가입 여부를 캐내는 걸 막으려고)
                .filter(Member::hasPassword)
                .filter(m -> passwordEncoder.matches(request.password(), m.getPasswordHash()))
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        return issue(member);
    }

    /** 구글 로그인: 구글 번호로 찾고 → 없으면 같은 이메일 회원에 이어 붙이고 → 그래도 없으면 새로 만든다 */
    @Transactional
    public String loginWithGoogle(String googleSub, String email, String name) {
        Member member = memberRepository.findByGoogleSub(googleSub)
                .orElseGet(() -> memberRepository.findByEmail(normalize(email))
                        .map(existing -> {
                            existing.linkGoogle(googleSub);
                            return existing;
                        })
                        .orElseGet(() -> memberRepository.save(Member.withGoogle(
                                normalize(email), googleSub, name == null || name.isBlank() ? "구글 사용자" : name))));
        return tokenProvider.issue(member.getId());
    }

    public MemberResponse me(Long memberId) {
        return memberRepository.findById(memberId)
                .map(MemberResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private TokenResponse issue(Member member) {
        return new TokenResponse(tokenProvider.issue(member.getId()), MemberResponse.from(member));
    }

    /** Test@Example.com 과 test@example.com 을 같은 사람으로 */
    private String normalize(String email) {
        return email.strip().toLowerCase();
    }
}
