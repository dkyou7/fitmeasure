package com.iamnot.fitmeasure.measurement.dto;

/** 결과지 한 줄: 이번 값 + 직전 값 대비 변화 */
public record ResultValueRow(
        Long itemId,
        String name,
        String unit,
        String displayValue,   // 표시용(TIME은 2:10 포맷)
        String changeLabel,    // "+5", "-12", "첫 측정" 등
        boolean improved,      // 개선 여부(방향 반영)
        boolean skipped
) {}