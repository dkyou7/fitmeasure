package com.iamnot.fitmeasure.measurement.session.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ResultView(
        Long sessionId,
        Long membershipId,
        String memberNickname,
        String programName,
        LocalDateTime measuredAt,
        List<ResultValueRow> rows
) {}