package com.iamnot.fitmeasure.measurement.session.dto;

import java.util.List;

public record MeasureForm(
        Long membershipId,
        String memberNickname,
        Long programId,
        String programName,
        List<MeasureItemInput> items
) {}