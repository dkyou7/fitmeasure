package com.iamnot.fitmeasure.member.dto;

import java.time.LocalDate;

public record MemberRow(
        Long membershipId,
        String nickname,
        String phone,
        boolean anonymous,
        LocalDate joinedAt
) {}