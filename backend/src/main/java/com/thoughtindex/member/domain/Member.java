package com.thoughtindex.member.domain;

import com.thoughtindex.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원. 이메일로 가입하면 passwordHash 가, 구글로 들어오면 googleSub 가 채워진다.
 * 같은 이메일이면 한 회원으로 본다 (이메일로 가입한 사람이 나중에 구글로 들어와도 같은 서랍).
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    /** 평문 비밀번호는 어디에도 저장하지 않는다. {bcrypt}... 형태의 해시만 */
    @Column(length = 100)
    private String passwordHash;

    /** 구글 계정 고유 번호 (OIDC 의 sub). 구글 로그인을 한 번도 안 했으면 null */
    @Column(unique = true, length = 64)
    private String googleSub;

    @Column(nullable = false, length = 30)
    private String name;

    private Member(String email, String passwordHash, String googleSub, String name) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.googleSub = googleSub;
        this.name = name;
    }

    public static Member withPassword(String email, String passwordHash, String name) {
        return new Member(email, passwordHash, null, name);
    }

    public static Member withGoogle(String email, String googleSub, String name) {
        return new Member(email, null, googleSub, name);
    }

    /** 이메일로 가입했던 회원이 처음 구글로 들어왔을 때 계정을 이어 붙인다 */
    public void linkGoogle(String googleSub) {
        this.googleSub = googleSub;
    }

    public boolean hasPassword() {
        return passwordHash != null;
    }
}
