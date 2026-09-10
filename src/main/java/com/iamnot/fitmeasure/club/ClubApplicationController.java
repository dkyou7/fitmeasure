package com.iamnot.fitmeasure.club;

import com.iamnot.fitmeasure.config.security.AppPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/apply")
@RequiredArgsConstructor
public class ClubApplicationController {

    private final ClubApplicationService applicationService;

    /** 사장 대상 소개 랜딩 (플랜·가치) — 신청 양식 진입 전 */
    @GetMapping("/intro")
    public String intro() {
        return "club/club-apply-intro";
    }

    @GetMapping
    public String applyForm(@AuthenticationPrincipal AppPrincipal principal, Model model) {
        if (principal == null) return "redirect:/login?next=/apply";
        if (applicationService.hasPending(principal.memberId())) {
            return "redirect:/apply/done";
        }
        model.addAttribute("types", ClubType.values());
        return "club/club-apply";
    }

    @PostMapping
    public String submit(@AuthenticationPrincipal AppPrincipal principal,
                         @RequestParam String name,
                         @RequestParam ClubType type,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String address,
                         Model model) {
        try {
            applicationService.apply(principal.memberId(), name, type, phone, address);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("types", ClubType.values());
            return "club/club-apply";
        }
        return "redirect:/apply/done";
    }

    @GetMapping("/done")
    public String done() {
        return "club/club-apply-done";
    }
}