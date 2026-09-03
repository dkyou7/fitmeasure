package com.iamnot.fitmeasure.member;

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
    public String feed(@AuthenticationPrincipal LoginMember loginMember, Model model) {
        if (loginMember == null) return "redirect:/login";
        model.addAttribute("feed", feedService.myFeed(loginMember));
        return "member/feed";
    }
}