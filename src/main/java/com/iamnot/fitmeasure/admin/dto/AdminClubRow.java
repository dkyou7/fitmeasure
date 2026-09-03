package com.iamnot.fitmeasure.admin.dto;

import java.time.LocalDate;

public record AdminClubRow(
        Long clubId,
        String name,
        String type,
        String plan,       // FREE / PAID
        long memberCount,
        long staffCount,
        LocalDate createdAt
) {}