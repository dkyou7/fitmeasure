package com.iamnot.fitmeasure.measurement;

import java.math.BigDecimal;

/** 측정값 표시 포맷 (TIME은 초→분:초) */
public final class MeasurementFormat {

    private MeasurementFormat() {}

    public static String display(MeasurementType type, BigDecimal number, String text) {
        if (type == MeasurementType.SELECT) {
            return text != null ? text : "-";
        }
        if (number == null) return "-";
        if (type == MeasurementType.TIME) {
            int total = number.intValue();
            return String.format("%d:%02d", total / 60, total % 60);
        }
        return number.stripTrailingZeros().toPlainString();
    }
}