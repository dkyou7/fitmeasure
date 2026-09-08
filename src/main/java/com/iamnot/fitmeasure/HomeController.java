package com.iamnot.fitmeasure;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.config.security.AppPrincipal;
import com.iamnot.fitmeasure.measurement.template.MeasurementTemplateRepository;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ClubRepository clubRepository;
    private final MembershipRepository membershipRepository;
    private final MeasurementTemplateRepository templateRepository;

    @GetMapping("/")
    public String root(@AuthenticationPrincipal AppPrincipal principal, Model model) {
        // 로그인 안 함 → 랜딩
        if (principal == null) return "landing";

        // 온보딩 먼저
        if (!principal.isOnboarded()) return "redirect:/onboarding";

        // 운영자 → 관리 화면
        if (principal.isPlatformAdmin()) return "redirect:/admin";

        // 클럽 소속 없음(회원·떠도는·카카오 신규) → 피드
        if (principal.clubId() == null) return "redirect:/me";

        // 클럽 소속(사장/트레이너) → 홈 대시보드
        Club club = clubRepository.findById(principal.clubId()).orElse(null);
        if (club == null) return "redirect:/me";

        long memberCount = membershipRepository.countClaimedMembers(principal.clubId());
        long programCount = templateRepository.findByClubId(principal.clubId()).size();
        model.addAttribute("clubName", club.getName());
        model.addAttribute("memberCount", memberCount);
        model.addAttribute("programCount", programCount);
        model.addAttribute("freeLimit", club.getFreeMemberLimit());
        model.addAttribute("overLimit", memberCount > club.getFreeMemberLimit());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}