package com.iamnot.fitmeasure.measurement.template.dto;

import com.iamnot.fitmeasure.measurement.template.FitnessCategory;
import com.iamnot.fitmeasure.measurement.template.MeasurementType;
import com.iamnot.fitmeasure.measurement.template.ScoreDirection;

/** 측정표 항목 행 (뷰 전용) */
public record TemplateItemRow(
        Long id,
        String name,
        MeasurementType measurementType,
        String unit,
        ScoreDirection direction,
        FitnessCategory category,
        int sortOrder,
        boolean active
) {}