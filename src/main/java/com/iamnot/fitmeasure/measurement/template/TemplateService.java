package com.iamnot.fitmeasure.measurement.template;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.measurement.session.MeasurementValueRepository;
import com.iamnot.fitmeasure.measurement.template.dto.ProgramRow;
import com.iamnot.fitmeasure.measurement.template.dto.TemplateItemRow;
import com.iamnot.fitmeasure.measurement.template.dto.TemplateView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    private com.iamnot.fitmeasure.club.Club clubReference() {
        // ClubRepository.getReferenceById 사용을 위해 주입 필요 — 아래 주석 참고
        throw new UnsupportedOperationException();
    }
}