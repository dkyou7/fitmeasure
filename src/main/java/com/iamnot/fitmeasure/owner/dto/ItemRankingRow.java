package com.iamnot.fitmeasure.owner.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 단일 항목 순위의 한 줄. rank는 1부터, 동점이면 같은 등수.
 * (측정표 전체를 가로로 펼치는 template.dto.RankingRow와는 다른 용도)
 */
public record ItemRankingRow(int rank,
                             Long membershipId,
                             String memberName,
                             BigDecimal value,
                             LocalDateTime measuredAt) {
}