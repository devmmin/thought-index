package com.thoughtindex.member.api.dto;

/** 로그인 성공 응답. 화면은 accessToken 을 저장해 두고 요청마다 Authorization 헤더에 싣는다 */
public record TokenResponse(String accessToken, MemberResponse member) {
}
