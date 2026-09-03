package com.iamnot.fitmeasure.member.dto;

/** claim 화면에 필요한 정보 (공유 토큰 경유) */
public record ClaimForm(
        String shareToken,
        String currentNickname,
        String clubName
) {}