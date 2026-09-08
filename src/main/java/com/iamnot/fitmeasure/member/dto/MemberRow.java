package com.iamnot.fitmeasure.member.dto;

import java.time.LocalDate;

public record MemberRow(
        Long membershipId,
        String nickname,
        LocalDate lastMeasuredDate,   // 마지막 측정일 (없으면 null)
        LocalDate joinedAt
) {}