package com.iamnot.fitmeasure.admin.dto;

public record AdminClubRow(
        Long clubId, String name, String type,
        String plan, String planLabel,
        String requestedPlan, String requestedLabel,
        java.time.LocalDate planExpiresAt,
        long memberCount, long staffCount,
        java.time.LocalDate createdAt
) {}