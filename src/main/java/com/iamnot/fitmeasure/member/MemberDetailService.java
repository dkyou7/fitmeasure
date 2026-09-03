package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.measurement.session.MeasurementSession;
import com.iamnot.fitmeasure.measurement.session.MeasurementSessionRepository;
import com.iamnot.fitmeasure.measurement.session.MeasurementValue;
import com.iamnot.fitmeasure.measurement.template.TemplateItem;
import com.iamnot.fitmeasure.measurement.session.dto.SessionSummary;
import com.iamnot.fitmeasure.measurement.session.dto.TrackedItem;
import com.iamnot.fitmeasure.member.dto.MemberDetail;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import com.iamnot.fitmeasure.membership.MembershipStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberDetailService {

    private final CurrentClub currentClub;
    private final MembershipRepository membershipRepository;
    private final MeasurementSessionRepository sessionRepository;

    @Transactional(readOnly = true)
    public MemberDetail getDetail(Long membershipId) {
        Membership m = membershipRepository
                .findByIdAndClubId(membershipId, currentClub.clubId())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        List<MeasurementSession> sessions = sessionRepository
                .findByMembershipIdOrderByMeasuredAtDesc(membershipId);

        // 이력 요약
        List<SessionSummary> summaries = sessions.stream()
                .map(s -> new SessionSummary(
                        s.getId(),
                        s.getTemplate().getName(),
                        s.getMeasuredAt(),
                        (int) s.getValues().stream().filter(v -> !v.isSkipped()).count()))
                .toList();

        // 추이 대상 항목 (측정된 적 있는 활성 항목, 중복 제거)
        Map<Long, TrackedItem> tracked = new LinkedHashMap<>();
        for (MeasurementSession s : sessions) {
            for (MeasurementValue v : s.getValues()) {
                if (v.isSkipped() || v.getValueNumber() == null) continue;
                TemplateItem item = v.getTemplateItem();
                tracked.putIfAbsent(item.getId(),
                        new TrackedItem(item.getId(), item.getName(), item.getUnit()));
            }
        }

        // 마지막 측정일 + 다음 예정일 (가장 최근 세션의 프로그램 주기 기준)
        LocalDate lastDate = null, nextDue = null;
        boolean overdue = false;
        if (!sessions.isEmpty()) {
            MeasurementSession latest = sessions.get(0); // 최신순 정렬
            lastDate = toDate(latest.getMeasuredAt());
            int cadence = latest.getTemplate().getRecommendedCadenceDays();
            nextDue = lastDate.plusDays(cadence);
            overdue = nextDue.isBefore(LocalDate.now());
        }

        return new MemberDetail(
                m.getId(), m.getNickname(), m.getMemberNo(),
                !m.getMember().isClaimed(),
                m.getStatus() == MembershipStatus.ACTIVE,
                m.getJoinedAt(),
                lastDate, nextDue, overdue,
                summaries, List.copyOf(tracked.values()));
    }

    /** 회원 활성/휴면 토글. claim한 회원은 본인 소유라 변경 불가 */
    @Transactional
    public void toggleStatus(Long membershipId) {
        Membership m = membershipRepository
                .findByIdAndClubId(membershipId, currentClub.clubId())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        if (m.getRole() != MembershipRole.MEMBER) {
            throw new IllegalStateException("회원만 상태를 변경할 수 있습니다.");
        }
        if (m.getMember().isClaimed()) {
            throw new IllegalStateException("가입한 회원은 휴면 처리할 수 없습니다. 본인이 관리합니다.");
        }
        if (m.getStatus() == MembershipStatus.ACTIVE) {
            m.deactivate();
        } else {
            m.activate();
        }
    }

    private LocalDate toDate(java.time.LocalDateTime dt) {
        return dt.atZone(ZoneId.systemDefault()).toLocalDate();
    }
}