package com.iamnot.fitmeasure.measurement.dto;

/** 이 회원이 측정한 적 있는 항목 (추이 그래프 대상) */
public record TrackedItem(
        Long itemId,
        String name,
        String unit
) {}