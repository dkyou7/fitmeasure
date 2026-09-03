package com.iamnot.fitmeasure.measurement.template.dto;

import java.util.List;

/** 측정표 화면 데이터 */
public record TemplateView(
        Long templateId,
        String name,
        int recommendedCadenceDays,
        List<TemplateItemRow> items
) {}