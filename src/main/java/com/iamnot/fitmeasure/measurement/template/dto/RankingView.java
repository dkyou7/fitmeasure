package com.iamnot.fitmeasure.measurement.template.dto;

import java.util.List;

/** 프로그램 순위표: 헤더(항목명) + 회원별 행 */
public record RankingView(
        List<String> itemNames,     // 가로 헤더 (활성 항목 순서)
        List<RankingRow> rows,      // 순위순 정렬된 회원 행
        boolean totalRankable       // 합계 종합등수 계산 가능 여부
) {}