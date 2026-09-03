package com.iamnot.fitmeasure.measurement.dashboard;

public enum MeasureStatus {
    NEVER,      // 측정 이력 없음
    NORMAL,     // 주기 내
    DUE_SOON,   // 예정일 임박(3일 내)
    OVERDUE,    // 예정일 지남
    AT_RISK     // 2주기 이상 미측정 (이탈 위험)
}