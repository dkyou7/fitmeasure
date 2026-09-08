package com.iamnot.fitmeasure.admin.dto;

import java.time.LocalDate;
import java.util.List;

public record AdminMemberRow(
        Long memberId,
        String name,
        String phone,
        List<String> usernames,
        List<String> socialProviders,   // hasSocial → 이걸로
        boolean platformAdmin,
        boolean onboarded,
        List<String> clubRoles,
        LocalDate createdAt
) {}