package com.iamnot.fitmeasure.member.dto;

import java.time.LocalDate;

/** 회원 목록 행. 뷰에 엔티티 대신 이걸 넘겨 지연 로딩을 피한다. */
public record MemberRow(
        Long membershipId,
        String nickname,
        String memberNo,
        boolean anonymous,
        LocalDate joinedAt
) {}