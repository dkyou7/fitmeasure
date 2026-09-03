package com.iamnot.fitmeasure.measurement.session.dto;

import java.time.LocalDate;
import java.util.List;

/** 회원이 공유받는 일회성 결과 카드 */
public record ShareCard(
        String clubName,
        String nickname,
        boolean anonymous,
        LocalDate measuredDate,
        List<ShareValueRow> values,
        boolean hasTrend        // 이전 기록 존재 여부(잠긴 그래프 티저 노출용)
) {
    public record ShareValueRow(
            String name,
            String displayValue,
            String unit,
            boolean skipped
    ) {}
}