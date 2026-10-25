package com.thoughtindex.member.api.dto;

import com.thoughtindex.member.domain.Member;

public record MemberResponse(Long id, String email, String name, boolean hasPassword, boolean hasGoogle) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail(), member.getName(),
                member.hasPassword(), member.getGoogleSub() != null);
    }
}
