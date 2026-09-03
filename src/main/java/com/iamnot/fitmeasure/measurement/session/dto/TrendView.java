package com.iamnot.fitmeasure.measurement.session.dto;

import java.util.List;

public record TrendView(
        String itemName,
        String unit,
        List<String> labels,    // 측정일 (yyyy.MM.dd)
        List<Double> values     // 값 (TIME도 숫자 그대로)
) {}