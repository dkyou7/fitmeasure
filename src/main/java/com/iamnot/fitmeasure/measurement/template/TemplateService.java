package com.iamnot.fitmeasure.measurement.template;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.measurement.session.MeasurementValueRepository;
import com.iamnot.fitmeasure.measurement.template.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final CurrentClub currentClub;
    private final MeasurementTemplateRepository templateRepository;
    private final ClubRepository clubRepository;
    private final MeasurementValueRepository valueRepository;

    /** 현재 클럽의 프로그램 목록 */
    @Transactional(readOnly = true)
    public List<ProgramRow> listPrograms() {
        return templateRepository.findByClubId(currentClub.clubId()).stream()
                .map(t -> new ProgramRow(
                        t.getId(), t.getName(),
                        (int) t.getItems().stream().filter(TemplateItem::isActive).count(),
                        t.getRecommendedCadenceDays()))
                .toList();
    }

    /** 빈 프로그램 새로 생성 */
    @Transactional
    public Long createProgram(String name, int cadenceDays) {
        Club club = clubRepository.getReferenceById(currentClub.clubId());
        MeasurementTemplate t = MeasurementTemplate.forClub(club, name, cadenceDays);
        return templateRepository.save(t).getId();
    }

    @Transactional(readOnly = true)
    public TemplateView getProgram(Long programId) {
        MeasurementTemplate t = load(programId);
        var rows = t.getItems().stream()
                .map(i -> new TemplateItemRow(
                        i.getId(), i.getName(), i.getMeasurementType(), i.getUnit(),
                        i.getDirection(), i.getCategory(), i.getSortOrder(), i.isActive(),
                        valueRepository.existsByTemplateItemId(i.getId())))   // locked
                .toList();
        return new TemplateView(t.getId(), t.getName(), t.getRecommendedCadenceDays(), rows);
    }

    /** 항목 수정. locked면 유형·단위·방향은 무시하고 이름·분류만 반영 */
    @Transactional
    public void updateItem(Long programId, Long itemId, String name,
                           MeasurementType type, String unit,
                           ScoreDirection direction, FitnessCategory category) {
        MeasurementTemplate t = load(programId);
        TemplateItem item = t.getItems().stream()
                .filter(i -> i.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("항목을 찾을 수 없습니다."));

        boolean locked = valueRepository.existsByTemplateItemId(itemId);
        if (locked) {
            item.editSafe(name, category);   // 이름·분류만
        } else {
            item.editAll(name, type, unit, direction, category);
        }
    }

    @Transactional
    public void addItem(Long programId, String name, MeasurementType type, String unit,
                        ScoreDirection direction, FitnessCategory category) {
        MeasurementTemplate t = load(programId);
        int nextOrder = t.getItems().stream()
                .mapToInt(TemplateItem::getSortOrder).max().orElse(-1) + 1;
        t.addItem(TemplateItem.builder()
                .name(name).measurementType(type).unit(unit)
                .direction(direction).category(category).sortOrder(nextOrder)
                .build());
    }

    @Transactional
    public void deactivateItem(Long programId, Long itemId) {
        load(programId).getItems().stream()
                .filter(i -> i.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("항목을 찾을 수 없습니다."))
                .deactivate();
    }

    private MeasurementTemplate load(Long programId) {
        return templateRepository.findByIdAndClubId(programId, currentClub.clubId())
                .orElseThrow(() -> new IllegalArgumentException("프로그램을 찾을 수 없습니다."));
    }
    /** 이 프로그램 기준 클럽 회원 순위표 */
    @Transactional(readOnly = true)
    public RankingView getRanking(Long programId) {
        MeasurementTemplate t = load(programId);
        Long clubId = currentClub.clubId();

        List<TemplateItem> items = t.getItems().stream()
                .filter(TemplateItem::isActive)
                .toList();

        List<String> itemNames = items.stream().map(TemplateItem::getName).toList();

        // 합계 등수 가능 조건: 항목 있고, 전부 NUMBER + HIGHER_BETTER + 같은 단위
        boolean totalRankable = !items.isEmpty()
                && items.stream().allMatch(i -> i.getMeasurementType() == MeasurementType.NUMBER
                && i.getDirection() == ScoreDirection.HIGHER_BETTER)
                && items.stream().map(i -> i.getUnit() == null ? "" : i.getUnit()).distinct().count() == 1;

        // 회원별 [항목index → 최신값] 수집
        Map<Long, String> nameByMember = new LinkedHashMap<>();
        Map<Long, BigDecimal[]> valuesByMember = new LinkedHashMap<>();

        for (int idx = 0; idx < items.size(); idx++) {
            var pop = valueRepository.findClubItemValues(clubId, programId, items.get(idx).getId());
            Set<Long> seen = new HashSet<>();          // 회원별 최신 1건만 (쿼리가 회원asc·측정일desc)
            for (var v : pop) {
                Long mid = v.getSession().getMembership().getId();
                if (!seen.add(mid)) continue;
                nameByMember.putIfAbsent(mid, v.getSession().getMembership().getNickname());
                valuesByMember.computeIfAbsent(mid, k -> new BigDecimal[items.size()])[idx] = v.getValueNumber();
            }
        }

        // 행 조립 + 합계 계산
        record Scored(Long memberId, BigDecimal total, boolean complete) {}
        List<Scored> scored = new ArrayList<>();
        for (var e : valuesByMember.entrySet()) {
            BigDecimal[] arr = e.getValue();
            boolean complete = Arrays.stream(arr).allMatch(Objects::nonNull);
            BigDecimal sum = Arrays.stream(arr).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            scored.add(new Scored(e.getKey(), sum, complete));
        }
        // 전 항목 측정한 회원 먼저, 그 안에서 합계 내림차순
        scored.sort(Comparator.comparing(Scored::complete).reversed()
                .thenComparing(Scored::total, Comparator.reverseOrder()));

        List<RankingRow> rows = new ArrayList<>();
        int rank = 0;
        for (Scored s : scored) {
            Integer r = null;
            if (totalRankable && s.complete()) r = ++rank;   // 미완은 등수 없음
            BigDecimal[] arr = valuesByMember.get(s.memberId());
            List<String> vals = Arrays.stream(arr)
                    .map(b -> b == null ? null : b.stripTrailingZeros().toPlainString())
                    .toList();
            rows.add(new RankingRow(r, nameByMember.get(s.memberId()), vals));
        }

        return new RankingView(itemNames, rows, totalRankable);
    }
}