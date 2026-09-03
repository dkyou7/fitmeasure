package com.iamnot.fitmeasure.member.dto;

import java.time.LocalDateTime;

public record FeedItem(
        Long sessionId,
        String clubName,
        String programName,
        LocalDateTime measuredAt,
        int itemCount
) {}