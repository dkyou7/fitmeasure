package com.iamnot.fitmeasure.measurement.template.dto;

import java.util.List;

/** 순위표 한 줄: 등수 + 회원명 + 항목별 값(헤더 순서와 일치, 미측정은 null) */
public record RankingRow(
        Integer rank,               // 종합 등수 (계산 불가·미완이면 null)
        String memberName,
        List<String> values         // 표시용 문자열, 미측정은 null
) {}