package com.iamnot.fitmeasure.measurement.dashboard.dto;

import com.iamnot.fitmeasure.measurement.dashboard.MeasureStatus;
import java.time.LocalDate;

public record DashboardRow(
        Long membershipId,
        String nickname,
        LocalDate lastMeasuredDate,   // null이면 이력 없음
        LocalDate nextDueDate,        // null이면 계산 불가
        MeasureStatus status,
        long daysOverdue              // 지연 일수(양수면 지남)
) {}