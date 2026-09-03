package com.iamnot.fitmeasure;

import com.iamnot.fitmeasure.config.security.LoginMember;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import com.iamnot.fitmeasure.measurement.template.MeasurementTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
    public String root(Authentication auth, Model model) {
        // 로그인 안 함 → 랜딩(소개)
        if (auth == null || !(auth.getPrincipal() instanceof LoginMember lm)) {
            return "landing";
        }

        if (lm.isPlatformAdmin()) {
            return "redirect:/admin";
        }
        Long clubId = lm.clubId();
        if (clubId == null) return "landing";
        var club = clubRepository.findById(clubId).orElse(null);
        long memberCount = membershipRepository.countClaimedMembers(clubId);
        long programCount = templateRepository.findByClubId(clubId).size();

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
}