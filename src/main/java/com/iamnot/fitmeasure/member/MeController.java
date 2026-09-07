package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.config.security.AppPrincipal;
import com.iamnot.fitmeasure.config.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MeController {

    private final MemberFeedService feedService;

    @GetMapping("/me")
    public String feed(@AuthenticationPrincipal AppPrincipal loginMember, Model model) {
        if (loginMember == null) return "redirect:/login";
        var feed = feedService.myFeed(loginMember);
        model.addAttribute("feed", feed);
        model.addAttribute("hasRecords", !feed.isEmpty());   // 측정 기록 있나
        return "member/feed";
    }
}