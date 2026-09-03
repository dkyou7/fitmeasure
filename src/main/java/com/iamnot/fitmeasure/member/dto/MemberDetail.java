package com.iamnot.fitmeasure.member.dto;

import com.iamnot.fitmeasure.measurement.dto.SessionSummary;
import com.iamnot.fitmeasure.measurement.dto.TrackedItem;

import java.time.LocalDate;
import java.util.List;

public record MemberDetail(
        Long membershipId,
        String nickname,
        String memberNo,
        boolean anonymous,
        LocalDate joinedAt,
        LocalDate lastMeasuredDate,   // null이면 측정 이력 없음
        LocalDate nextDueDate,        // null이면 계산 불가
        boolean overdue,              // 예정일 지남
        List<SessionSummary> sessions,
        List<TrackedItem> trackedItems
) {}