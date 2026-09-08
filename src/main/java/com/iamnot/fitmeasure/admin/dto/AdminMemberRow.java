package com.iamnot.fitmeasure.admin.dto;

import java.time.LocalDate;
import java.util.List;

public record AdminMemberRow(
        Long memberId,
        String name,
        String phone,
        List<String> usernames,
        boolean hasSocial,
        boolean platformAdmin,
        boolean onboarded,
        List<String> clubRoles,   // "클럽명 (OWNER)" 형태
        LocalDate createdAt
) {}