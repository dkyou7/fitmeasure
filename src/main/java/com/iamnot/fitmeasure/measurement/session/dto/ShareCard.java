package com.iamnot.fitmeasure.measurement.session.dto;

import java.time.LocalDate;
import java.util.List;

/** 회원이 공유받는 일회성 결과 카드 */
public record ShareCard(
        String shareToken,
        String clubName,
        String measuredByName,   // 측정한 트레이너 이름 추가
        String nickname,
        boolean anonymous,
        LocalDate measuredDate,
        List<ShareValueRow> values,
        boolean hasTrend
) {
    public record ShareValueRow(String name, String displayValue, String unit, boolean skipped) {}
}