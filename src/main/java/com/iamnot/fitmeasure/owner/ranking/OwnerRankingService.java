package com.iamnot.fitmeasure.owner.ranking;

import com.iamnot.fitmeasure.measurement.template.MeasurementType;
import com.iamnot.fitmeasure.measurement.template.ScoreDirection;
import com.iamnot.fitmeasure.measurement.template.TemplateItem;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.owner.dto.ItemRankingRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OwnerRankingService {

    private final RankingQueryRepository rankingQueryRepository;

    @Transactional(readOnly = true)
    public List<TemplateItem> rankableItems(Long clubId) {
        return rankingQueryRepository.findRankableItems(clubId, MeasurementType.SELECT);
    }

    /**
     * 회원별 최고 기록 순위.
     * 같은 회원이 여러 번 쟀으면 항목 방향(direction)에 맞는 최고값 1건만 남긴다.
     */
    @Transactional(readOnly = true)
    public List<ItemRankingRow> absoluteRanking(Long clubId, TemplateItem item) {
        boolean higherBetter = item.getDirection() == ScoreDirection.HIGHER_BETTER;

        Map<Long, Object[]> bestByMember = new LinkedHashMap<>();
        for (Object[] row : rankingQueryRepository.findNumericValues(clubId, item.getId())) {
            Long membershipId = (Long) row[0];
            BigDecimal value = (BigDecimal) row[2];

            Object[] current = bestByMember.get(membershipId);
            if (current == null || isBetter(value, (BigDecimal) current[2], higherBetter)) {
                bestByMember.put(membershipId, row);
            }
        }

        List<Object[]> sorted = new ArrayList<>(bestByMember.values());
        sorted.sort((a, b) -> {
            int cmp = ((BigDecimal) a[2]).compareTo((BigDecimal) b[2]);
            if (cmp != 0) return higherBetter ? -cmp : cmp;
            // 동점이면 먼저 달성한 쪽을 앞으로
            return ((LocalDateTime) a[3]).compareTo((LocalDateTime) b[3]);
        });

        List<ItemRankingRow> result = new ArrayList<>(sorted.size());
        BigDecimal prevValue = null;
        int prevRank = 0;
        for (int i = 0; i < sorted.size(); i++) {
            Object[] row = sorted.get(i);
            BigDecimal value = (BigDecimal) row[2];
            int rank = (prevValue != null && prevValue.compareTo(value) == 0) ? prevRank : i + 1;
            result.add(new ItemRankingRow(rank, (Long) row[0], (String) row[1],
                    value, (LocalDateTime) row[3]));
            prevValue = value;
            prevRank = rank;
        }
        return result;
    }

    private boolean isBetter(BigDecimal candidate, BigDecimal current, boolean higherBetter) {
        int cmp = candidate.compareTo(current);
        return higherBetter ? cmp > 0 : cmp < 0;
    }
}