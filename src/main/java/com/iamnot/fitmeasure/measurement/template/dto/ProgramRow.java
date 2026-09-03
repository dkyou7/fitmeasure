package com.iamnot.fitmeasure.measurement.template.dto;

/** 프로그램 목록 행 */
public record ProgramRow(
        Long id,
        String name,
        int itemCount,
        int recommendedCadenceDays
) {}