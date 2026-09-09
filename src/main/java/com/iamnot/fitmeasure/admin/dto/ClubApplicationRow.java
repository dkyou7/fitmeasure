package com.iamnot.fitmeasure.admin.dto;

import com.iamnot.fitmeasure.club.ClubType;
import java.time.LocalDate;

public record ClubApplicationRow(
        Long clubId,
        String name,
        String type,
        String phone,
        String address,
        String applicantName,
        LocalDate appliedAt
) {}