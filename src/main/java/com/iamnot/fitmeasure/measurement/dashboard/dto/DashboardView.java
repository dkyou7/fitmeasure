package com.iamnot.fitmeasure.measurement.dashboard.dto;

import java.util.List;

public record DashboardView(
        int total,
        long attentionCount,   // 측정 권할 회원 수(OVERDUE+AT_RISK+DUE_SOON)
        List<DashboardRow> attention,  // 측정 권할 회원 (우선순위순)
        List<DashboardRow> normal      // 정상/신규
) {}