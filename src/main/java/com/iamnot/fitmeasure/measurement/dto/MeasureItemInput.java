package com.iamnot.fitmeasure.measurement.dto;

import com.iamnot.fitmeasure.measurement.MeasurementType;

/** 측정 입력 화면의 항목 한 줄 */
public record MeasureItemInput(
        Long itemId,
        String name,
        MeasurementType measurementType,
        String unit,
        String protocol,
        String selectOptions
) {}