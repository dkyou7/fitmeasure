package com.iamnot.fitmeasure.home;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.measurement.dashboard.DashboardService;
import com.iamnot.fitmeasure.measurement.dashboard.dto.DashboardRow;
import com.iamnot.fitmeasure.measurement.session.MeasurementSessionRepository;
import com.iamnot.fitmeasure.measurement.template.MeasurementTemplateRepository;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final ClubRepository clubRepository;
    private final MembershipRepository membershipRepository;
    private final MeasurementTemplateRepository templateRepository;
    private final MeasurementSessionRepository sessionRepository;
    private final DashboardService dashboardService;   // 측정 명단 재사용

    @Transactional(readOnly = true)
    public OwnerHome ownerHome(Long clubId) {
        Club club = clubRepository.findById(clubId).orElseThrow();

        long memberCount = membershipRepository.countClaimedMembers(clubId);
        long programCount = templateRepository.findByClubId(clubId).size();
        int freeLimit = club.getFreeMemberLimit();

        // 이번 주 측정 횟수
        LocalDateTime weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
        long weekMeasures = sessionRepository.countByClubIdAndMeasuredAtAfter(clubId, weekStart);

        // 챙길 회원 (측정 명단에서 상위)
        var dash = dashboardService.getDashboard();   // 현재 클럽 기준
        var attention = dash.attention().stream().limit(5).toList();

        return new OwnerHome(
                club.getName(), memberCount, programCount, freeLimit,
                memberCount > freeLimit, weekMeasures,
                dash.attentionCount(), attention);
    }

    public record OwnerHome(
            String clubName, long memberCount, long programCount, int freeLimit,
            boolean overLimit, long weekMeasures,
            long attentionCount, List<DashboardRow> attention) {}
}