package com.iamnot.fitmeasure.onboard;

import com.iamnot.fitmeasure.config.security.AppPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping("/onboarding")
    public String form(@AuthenticationPrincipal AppPrincipal principal, Model model) {
        if (principal == null) return "redirect:/login";
        if (principal.isOnboarded()) return "redirect:/me";  // 이미 했으면 skip
        model.addAttribute("nickname", principal.nickname());  // 카카오 닉네임 기본값
        return "onboarding";
    }

    @PostMapping("/onboarding")
    public String submit(@AuthenticationPrincipal AppPrincipal principal,
                         @RequestParam String name,
                         @RequestParam(required = false) String phone) {
        onboardingService.complete(principal.memberId(), name, phone);
        return "redirect:/me";
    }
}