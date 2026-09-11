package com.iamnot.fitmeasure.measurement.session.dto;

/** 결과지 한 줄: 이번 값 + 직전 값 대비 변화 */
public record ResultValueRow(
        Long itemId,
        String name,
        String unit,
        String displayValue,
        String changeLabel,
        boolean improved,
        boolean skipped,
        Integer rank,        // 클럽 내 순위 (1위=최상위, 계산 불가면 null)
        Integer rankTotal    // 모집단 인원수 (분모)
) {}