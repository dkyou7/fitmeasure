package com.iamnot.fitmeasure.member.dto;

import com.iamnot.fitmeasure.measurement.session.dto.SessionSummary;
import com.iamnot.fitmeasure.measurement.session.dto.TrackedItem;

import java.time.LocalDate;
import java.util.List;

public record MemberDetail(
        Long membershipId,
        String nickname,
        boolean active,
        LocalDate joinedAt,
        LocalDate lastMeasuredDate,
        LocalDate nextDueDate,
        boolean overdue,
        List<SessionSummary> sessions,
        List<TrackedItem> trackedItems
) {}