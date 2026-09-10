package com.iamnot.fitmeasure.member.dto;

import com.iamnot.fitmeasure.measurement.session.dto.SessionSummary;
import com.iamnot.fitmeasure.measurement.session.dto.TrackedItem;

import java.util.List;

public record ClubRecordDetail(Long membershipId, String clubName, int sessionCount,
                               List<TrackedItem> trackedItems, List<SessionSummary> sessions) {}