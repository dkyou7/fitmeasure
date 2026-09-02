package com.iamnot.fitmeasure.measurement;

import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.measurement.dto.TemplateItemRow;
import com.iamnot.fitmeasure.measurement.dto.TemplateView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final CurrentClub currentClub;
    private final MeasurementTemplateRepository templateRepository;

    /** 현재 클럽의 기본 측정표 조회 (활성 항목만 표시, 비활성은 흐리게) */
    @Transactional(readOnly = true)
    public TemplateView getDefaultTemplate() {
        MeasurementTemplate t = loadDefaultTemplate();
        var rows = t.getItems().stream()
                .map(i -> new TemplateItemRow(
                        i.getId(), i.getName(), i.getMeasurementType(), i.getUnit(),
                        i.getDirection(), i.getCategory(), i.getSortOrder(), i.isActive()))
                .toList();
        return new TemplateView(t.getId(), t.getName(), t.getRecommendedCadenceDays(), rows);
    }

    /** 항목 추가 */
    @Transactional
    public void addItem(String name, MeasurementType type, String unit,
                        ScoreDirection direction, FitnessCategory category) {
        MeasurementTemplate t = loadDefaultTemplate();
        int nextOrder = t.getItems().stream()
                .mapToInt(TemplateItem::getSortOrder).max().orElse(-1) + 1;
        t.addItem(TemplateItem.builder()
                .name(name).measurementType(type).unit(unit)
                .direction(direction).category(category)
                .sortOrder(nextOrder)
                .build());
        // cascade로 저장됨 (트랜잭션 dirty checking)
    }

    /** 항목 비활성 (삭제 아님 — 과거 측정값 보존) */
    @Transactional
    public void deactivateItem(Long itemId) {
        MeasurementTemplate t = loadDefaultTemplate();
        t.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("항목을 찾을 수 없습니다."))
                .deactivate();
    }

    private MeasurementTemplate loadDefaultTemplate() {
        return templateRepository.findByClubIdAndIsDefaultTrue(currentClub.clubId())
                .orElseThrow(() -> new IllegalStateException("클럽 기본 측정표가 없습니다."));
    }
}