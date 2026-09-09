package com.iamnot.fitmeasure.member.dto;

import java.time.LocalDate;

public record ClubSummary(Long membershipId, String clubName,
                          LocalDate lastMeasured, int sessionCount) {}