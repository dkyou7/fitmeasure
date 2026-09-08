package com.iamnot.fitmeasure.measurement.session;

import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.config.security.AppPrincipal;
import com.iamnot.fitmeasure.config.security.LoginMember;
import com.iamnot.fitmeasure.measurement.session.dto.*;
import com.iamnot.fitmeasure.measurement.template.*;
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
    private final ShareTokenGenerator shareTokenGenerator;

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
                     Map<Long, String> values, String note, AppPrincipal loginMember) {
        Membership member = loadMember(membershipId);
        MeasurementTemplate program = loadProgram(programId);
        Membership measuredBy = currentMeasurer(loginMember);

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

    /** 측정자 = 현재 로그인 사용자의 이 클럽 멤버십 (STAFF 또는 OWNER) */
    private Membership currentMeasurer(AppPrincipal loginMember) {
        Membership measurer = membershipRepository
                .findByMemberIdAndClubId(loginMember.memberId(), currentClub.clubId())
                .orElseThrow(() -> new IllegalStateException("이 클럽의 측정 권한이 없습니다."));
        if (measurer.getRole() != MembershipRole.STAFF
                && measurer.getRole() != MembershipRole.OWNER) {
            throw new IllegalStateException("측정 권한이 없는 사용자입니다.");
        }
        return measurer;
    }

    /** 측정 결과지: 이번 세션 + 직전 세션 대비 변화 */
    @Transactional(readOnly = true)
    public ResultView getResult(Long sessionId, AppPrincipal loginMember) {
        MeasurementSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("측정 기록을 찾을 수 없습니다."));

        Membership membership = session.getMembership();
        Long sessionClubId = membership.getClub().getId();
        Long sessionMemberId = membership.getMember().getId();

        boolean isOwnerOfRecord = sessionMemberId.equals(loginMember.memberId());  // 회원 본인
        boolean isSameClub = loginMember.clubId() != null
                && loginMember.clubId().equals(sessionClubId);                     // 그 클럽 트레이너/사장

        if (!isOwnerOfRecord && !isSameClub) {
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
    public TrendView getTrend(Long membershipId, Long itemId, AppPrincipal loginMember) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        boolean isOwner = membership.getMember().getId().equals(loginMember.memberId());
        boolean isSameClub = loginMember.clubId() != null
                && loginMember.clubId().equals(membership.getClub().getId());
        if (!isOwner && !isSameClub) {
            throw new IllegalArgumentException("접근할 수 없습니다.");
        }
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

    /** 세션 공유 활성화 → 토큰 반환 (트레이너가 "공유하기" 누를 때) */
    @Transactional
    public String enableShare(Long sessionId) {
        MeasurementSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("측정 기록을 찾을 수 없습니다."));
        if (!session.getMembership().getClub().getId().equals(currentClub.clubId())) {
            throw new IllegalArgumentException("접근할 수 없는 기록입니다.");
        }
        if (session.getShareToken() == null) {
            session.enableShare(shareTokenGenerator.generate());
        } else {
            session.enableShare(session.getShareToken()); // 이미 있으면 재활성만
        }
        return session.getShareToken();
    }

    /** 공개 카드 조회 (로그인 불필요, 토큰으로만) */
    @Transactional(readOnly = true)
    public ShareCard getShareCard(String token) {
        MeasurementSession session = sessionRepository
                .findByShareTokenAndShareEnabledTrue(token)
                .orElseThrow(() -> new IllegalArgumentException("공유된 기록을 찾을 수 없습니다."));

        Membership member = session.getMembership();
        Long membershipId = member.getId();

        List<ShareCard.ShareValueRow> rows = session.getValues().stream().map(v -> {
            TemplateItem item = v.getTemplateItem();
            String display = v.isSkipped() ? "-"
                    : MeasurementFormat.display(item.getMeasurementType(),
                    v.getValueNumber(), v.getValueText());
            return new ShareCard.ShareValueRow(item.getName(), display,
                    item.getUnit(), v.isSkipped());
        }).toList();

        // 이 회원에게 2회 이상 측정 이력이 있으면 추이가 존재 → 잠긴 그래프 티저
        boolean hasTrend = sessionRepository
                .findByMembershipIdOrderByMeasuredAtDesc(membershipId).size() > 1;
        Membership measurer = session.getMeasuredBy();
        String measuredByName = measurer != null ? measurer.getNickname() : null;

        return new ShareCard(
                token,
                member.getClub().getName(),
                measuredByName,              // 추가
                member.getNickname(),
                !member.getMember().isClaimed(),
                session.getMeasuredAt().toLocalDate(),
                rows, hasTrend);
    }
}