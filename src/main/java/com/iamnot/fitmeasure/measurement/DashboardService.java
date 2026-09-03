package com.iamnot.fitmeasure.measurement;

import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.measurement.dto.DashboardRow;
import com.iamnot.fitmeasure.measurement.dto.DashboardView;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int DUE_SOON_DAYS = 3;

    private final CurrentClub currentClub;
    private final MembershipRepository membershipRepository;
    private final MeasurementSessionRepository sessionRepository;

    @Transactional(readOnly = true)
    public DashboardView getDashboard() {
        Long clubId = currentClub.clubId();
        LocalDate today = LocalDate.now();

        List<Membership> members = membershipRepository
                .findByClubIdAndRoleOrderByNicknameAsc(clubId, MembershipRole.MEMBER);

        List<DashboardRow> attention = new ArrayList<>();
        List<DashboardRow> normal = new ArrayList<>();

        for (Membership m : members) {
            Optional<MeasurementSession> latestOpt = sessionRepository
                    .findFirstByMembershipIdOrderByMeasuredAtDesc(m.getId());

            if (latestOpt.isEmpty()) {
                normal.add(new DashboardRow(m.getId(), m.getNickname(),
                        null, null, MeasureStatus.NEVER, 0));
                continue;
            }

            MeasurementSession latest = latestOpt.get();
            LocalDate lastDate = latest.getMeasuredAt()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            int cadence = latest.getTemplate().getRecommendedCadenceDays();
            LocalDate nextDue = lastDate.plusDays(cadence);
            long overdueDays = ChronoUnit.DAYS.between(nextDue, today); // 양수면 지남

            MeasureStatus status;
            if (overdueDays >= cadence) {
                status = MeasureStatus.AT_RISK;        // 2주기 이상
            } else if (overdueDays > 0) {
                status = MeasureStatus.OVERDUE;
            } else if (overdueDays >= -DUE_SOON_DAYS) {
                status = MeasureStatus.DUE_SOON;       // 3일 내 임박
            } else {
                status = MeasureStatus.NORMAL;
            }

            DashboardRow row = new DashboardRow(m.getId(), m.getNickname(),
                    lastDate, nextDue, status, Math.max(overdueDays, 0));

            if (status == MeasureStatus.OVERDUE || status == MeasureStatus.AT_RISK
                    || status == MeasureStatus.DUE_SOON) {
                attention.add(row);
            } else {
                normal.add(row);
            }
        }

        // 측정 권할 회원: 지연 오래된 순 → 위험도 순
        attention.sort(Comparator
                .comparing((DashboardRow r) -> statusPriority(r.status()))
                .thenComparing(Comparator.comparingLong(DashboardRow::daysOverdue).reversed()));

        return new DashboardView(members.size(), attention.size(), attention, normal);
    }

    private int statusPriority(MeasureStatus s) {
        return switch (s) {
            case AT_RISK -> 0;
            case OVERDUE -> 1;
            case DUE_SOON -> 2;
            default -> 3;
        };
    }
}