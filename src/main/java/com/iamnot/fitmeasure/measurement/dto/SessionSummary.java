package com.iamnot.fitmeasure.measurement.dto;

import java.time.LocalDateTime;

/** 측정 이력 한 줄 */
public record SessionSummary(
        Long sessionId,
        String programName,
        LocalDateTime measuredAt,
        int itemCount
) {}