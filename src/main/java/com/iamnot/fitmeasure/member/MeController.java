package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.club.ClubDiscoveryService;
import com.iamnot.fitmeasure.config.security.AppPrincipal;
import com.iamnot.fitmeasure.config.security.LoginMember;
import com.iamnot.fitmeasure.membership.ConnectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MeController {

    private final MemberFeedService feedService;
    private final ConnectService connectService;
    private final ClubDiscoveryService clubDiscoveryService;

    @GetMapping("/me")
    public String home(@AuthenticationPrincipal AppPrincipal principal, Model model) {
        if (principal == null) return "redirect:/login";
        model.addAttribute("clubs", clubDiscoveryService.listedClubs());
        model.addAttribute("recentFeed", feedService.recentFeed(principal, 3));  // 최근 3개
        model.addAttribute("activeTab", "home");
        return "member/home";
    }

    /** 내 기록 (전체 측정 히스토리) */
    @GetMapping("/me/records")
    public String records(@AuthenticationPrincipal AppPrincipal principal, Model model) {
        if (principal == null) return "redirect:/login";
        model.addAttribute("feed", feedService.myFeed(principal));
        model.addAttribute("activeTab", "records");
        return "member/feed";   // 기존 피드 화면 재사용
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
}