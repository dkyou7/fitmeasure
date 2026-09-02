package com.iamnot.fitmeasure.measurement;

/** 측정 항목의 입력 유형. 입력 위젯과 값 검증 방식이 갈린다. */
public enum MeasurementType {
    NUMBER,   // 무게(kg), 거리(cm) 등
    REPS,     // 횟수
    TIME,     // 시간(초 단위로 저장)
    SELECT    // 선택지 중 택1
}