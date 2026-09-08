package com.iamnot.fitmeasure.club.dto;

public record ClubListRow(
        Long clubId,
        String name,
        String address,
        String type
) {}