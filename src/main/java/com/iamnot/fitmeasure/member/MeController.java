package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.club.ClubApplicationService;
import com.iamnot.fitmeasure.club.ClubDiscoveryService;
import com.iamnot.fitmeasure.config.security.AppPrincipal;
import com.iamnot.fitmeasure.membership.ConnectService;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class MeController {

    private final MemberFeedService feedService;
    private final ConnectService connectService;
    private final ClubDiscoveryService clubDiscoveryService;
    private final MembershipRepository membershipRepository;
    private final ClubApplicationService applicationService;

    @GetMapping("/me")
    public String home(@AuthenticationPrincipal AppPrincipal principal, Model model) {
        if (principal == null) return "redirect:/login";
        model.addAttribute("clubs", clubDiscoveryService.listedClubs());
        model.addAttribute("recentFeed", feedService.recentFeed(principal, 3));
        // 오픈 신청 배너: 클럽 소속(OWNER/STAFF) 없고, 신청 대기도 없을 때만
        boolean canApply = !membershipRepository.hasOwnerOrStaffRole(principal.memberId())
                && !applicationService.hasPending(principal.memberId());
        model.addAttribute("showApplyBanner", canApply);
        model.addAttribute("activeTab", "home");
        return "member/home";
    }

    /** 내 기록 (전체 측정 히스토리) */
    @GetMapping("/me/records")
    public String records(@AuthenticationPrincipal AppPrincipal principal, Model model) {
        if (principal == null) return "redirect:/login";
        model.addAttribute("clubs", feedService.myClubs(principal));
        model.addAttribute("activeTab", "records");
        return "member/records-clubs";
    }

    @GetMapping("/me/connect")
    public String connectCode(@AuthenticationPrincipal AppPrincipal principal, Model model) {
        if (principal == null) return "redirect:/login";
        model.addAttribute("code", connectService.issueCode(principal));
        model.addAttribute("activeTab", "connect");
        return "member/connect-code";
    }

    @GetMapping("/me/gyms")
    public String gyms(@AuthenticationPrincipal AppPrincipal principal, Model model) {
        if (principal == null) return "redirect:/login";
        model.addAttribute("clubs", clubDiscoveryService.listedClubs());
        model.addAttribute("activeTab", "gyms");
        return "member/gyms";
    }

    @GetMapping("/me/records/{membershipId}")
    public String clubRecord(@AuthenticationPrincipal AppPrincipal principal,
                             @PathVariable Long membershipId, Model model) {
        if (principal == null) return "redirect:/login";
        model.addAttribute("detail", feedService.clubDetail(principal, membershipId));
        model.addAttribute("activeTab", "records");
        return "member/records-detail";
    }
}