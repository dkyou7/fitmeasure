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
    public String feed(@AuthenticationPrincipal AppPrincipal loginMember, Model model) {
        if (loginMember == null) return "redirect:/login";
        var feed = feedService.myFeed(loginMember);
        model.addAttribute("feed", feed);
        model.addAttribute("hasRecords", !feed.isEmpty());
        model.addAttribute("activeTab", "feed");
        return "member/feed";
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