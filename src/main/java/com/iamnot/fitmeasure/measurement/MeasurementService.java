package com.iamnot.fitmeasure.measurement;

import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.measurement.dto.*;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MeasurementService {

    private final CurrentClub currentClub;
    private final MembershipRepository membershipRepository;
    private final MeasurementTemplateRepository templateRepository;
    private final MeasurementSessionRepository sessionRepository;
    private final MeasurementValueRepository valueRepository;

    /** 측정 화면 데이터 구성: 회원 + 프로그램의 활성 항목 */
    @Transactional(readOnly = true)
    public MeasureForm prepareForm(Long membershipId, Long programId) {
        Membership member = loadMember(membershipId);
        MeasurementTemplate program = loadProgram(programId);

        List<MeasureItemInput> inputs = program.getItems().stream()
                .filter(TemplateItem::isActive)
                .map(i -> new MeasureItemInput(
                        i.getId(), i.getName(), i.getMeasurementType(),
                        i.getUnit(), i.getProtocol(), i.getSelectOptions()))
                .toList();

        return new MeasureForm(
                member.getId(), member.getNickname(),
                program.getId(), program.getName(), inputs);
    }

    /**
     * 측정 저장. values: itemId → 입력 문자열.
     * 빈 값은 skipped 처리. TIME은 초 단위 숫자로 들어온다고 가정(화면에서 변환).
     */
    @Transactional
    public Long save(Long membershipId, Long programId,
                     Map<Long, String> values, String note) {
        Membership member = loadMember(membershipId);
        MeasurementTemplate program = loadProgram(programId);
        Membership measuredBy = currentMeasurer();

        MeasurementSession session = new MeasurementSession(
                member, program, measuredBy, LocalDateTime.now());
        if (note != null && !note.isBlank()) {
            session.updateNote(note.trim());
        }

        for (TemplateItem item : program.getItems()) {
            if (!item.isActive()) continue;
            String raw = values.get(item.getId());

            if (raw == null || raw.isBlank()) {
                session.addValue(MeasurementValue.skipped(item));
                continue;
            }
            if (item.getMeasurementType() == MeasurementType.SELECT) {
                session.addValue(MeasurementValue.ofText(item, raw.trim()));
            } else {
                session.addValue(MeasurementValue.ofNumber(item, new BigDecimal(raw.trim())));
            }
        }

        return sessionRepository.save(session).getId();
    }

    private Membership loadMember(Long membershipId) {
        Membership m = membershipRepository
                .findByIdAndClubId(membershipId, currentClub.clubId())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        if (m.getRole() != MembershipRole.MEMBER) {
            throw new IllegalArgumentException("측정 대상 회원이 아닙니다.");
        }
        return m;
    }

    private MeasurementTemplate loadProgram(Long programId) {
        return templateRepository.findByIdAndClubId(programId, currentClub.clubId())
                .orElseThrow(() -> new IllegalArgumentException("프로그램을 찾을 수 없습니다."));
    }

    /** 임시: 측정자를 클럽의 STAFF/OWNER 아무나로. Security 붙으면 로그인 사용자로 교체 */
    private Membership currentMeasurer() {
        return membershipRepository
                .findByClubIdAndRoleOrderByNicknameAsc(currentClub.clubId(), MembershipRole.STAFF)
                .stream().findFirst()
                .or(() -> membershipRepository
                        .findByClubIdAndRoleOrderByNicknameAsc(currentClub.clubId(), MembershipRole.OWNER)
                        .stream().findFirst())
                .orElseThrow(() -> new IllegalStateException("측정자(STAFF/OWNER)가 없습니다."));
    }

    /** 측정 결과지: 이번 세션 + 직전 세션 대비 변화 */
    @Transactional(readOnly = true)
    public ResultView getResult(Long sessionId) {
        MeasurementSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("측정 기록을 찾을 수 없습니다."));
        // 격리 검증: 세션의 클럽이 현재 클럽인지
        Long clubId = session.getMembership().getClub().getId();
        if (!clubId.equals(currentClub.clubId())) {
            throw new IllegalArgumentException("접근할 수 없는 기록입니다.");
        }

        Long membershipId = session.getMembership().getId();

        List<ResultValueRow> rows = session.getValues().stream().map(v -> {
            TemplateItem item = v.getTemplateItem();
            String display = v.isSkipped() ? "-"
                    : MeasurementFormat.display(item.getMeasurementType(),
                    v.getValueNumber(), v.getValueText());
            // 직전 값과 비교
            String change = "첫 측정";
            boolean improved = false;
            if (!v.isSkipped() && v.getValueNumber() != null) {
                var trend = valueRepository.findTrend(membershipId, item.getId());
                // trend는 측정일 오름차순. 현재 세션 값의 직전을 찾음
                BigDecimal prev = null;
                for (MeasurementValue tv : trend) {
                    if (tv.getSession().getId().equals(sessionId)) break;
                    if (tv.getValueNumber() != null) prev = tv.getValueNumber();
                }
                if (prev != null) {
                    BigDecimal diff = v.getValueNumber().subtract(prev);
                    int cmp = diff.signum();
                    boolean higherBetter = item.getDirection() == ScoreDirection.HIGHER_BETTER;
                    improved = higherBetter ? cmp > 0 : cmp < 0;
                    change = (cmp > 0 ? "+" : "") + diff.stripTrailingZeros().toPlainString();
                }
            }
            return new ResultValueRow(item.getId(), item.getName(), item.getUnit(),
                    display, v.isSkipped() ? "-" : change, improved, v.isSkipped());
        }).toList();

        return new ResultView(session.getId(), membershipId,
                session.getMembership().getNickname(),
                session.getTemplate().getName(),
                session.getMeasuredAt(), rows);
    }

    /** 특정 항목의 성장 추이 */
    @Transactional(readOnly = true)
    public TrendView getTrend(Long membershipId, Long itemId) {
        // 격리 검증
        membershipRepository.findByIdAndClubId(membershipId, currentClub.clubId())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        var values = valueRepository.findTrend(membershipId, itemId);
        List<String> labels = new java.util.ArrayList<>();
        List<Double> nums = new java.util.ArrayList<>();
        String itemName = "";
        String unit = "";
        for (MeasurementValue v : values) {
            if (v.getValueNumber() == null) continue;
            labels.add(v.getSession().getMeasuredAt()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yy.MM.dd")));
            nums.add(v.getValueNumber().doubleValue());
            itemName = v.getTemplateItem().getName();
            unit = v.getTemplateItem().getUnit();
        }
        return new TrendView(itemName, unit, labels, nums);
    }
}