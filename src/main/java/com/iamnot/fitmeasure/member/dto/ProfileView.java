package com.iamnot.fitmeasure.member.dto;

public record ProfileView(
        String username,      // 로그인 아이디 (USERNAME credential)
        String name,
        String nickname,
        String phone,
        boolean hasClub,
        boolean hasPhone      // 전화번호 연결 여부(통합 가능 여부)
) {}