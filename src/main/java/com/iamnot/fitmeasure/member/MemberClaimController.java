package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.config.security.LoginMember;
import com.iamnot.fitmeasure.member.dto.ClaimForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MemberClaimController {

    private final MemberClaimService claimService;

    /** claim 폼 (공유 카드에서 진입) */
    @GetMapping("/s/{token}/claim")
    public String form(@PathVariable String token, Model model) {
        model.addAttribute("form", claimService.prepareForm(token));
        return "share/claim";
    }

    @PostMapping("/s/{token}/claim")
    public String submit(@PathVariable String token,
                         @RequestParam String username,
                         @RequestParam String password,
                         Model model) {
        try {
            claimService.claimByUsername(token, username, password);
        } catch (IllegalStateException e) {
            model.addAttribute("form", claimService.prepareForm(token));
            model.addAttribute("error", e.getMessage());
            return "share/claim";
        }
        return "redirect:/s/" + token + "/claim/done";
    }

    @GetMapping("/s/{token}/claim/done")
    public String done(@PathVariable String token, Model model) {
        model.addAttribute("form", claimService.prepareForm(token));
        return "share/claim-done";
    }

    /** 로그인 상태로 이 세션을 내 계정에 흡수 */
    @PostMapping("/s/{token}/absorb")
    public String absorb(@PathVariable String token,
                         @AuthenticationPrincipal LoginMember loginMember,
                         Model model) {
        if (loginMember == null) {
            return "redirect:/login";   // 로그인 먼저
        }
        try {
            claimService.absorbToLoggedIn(token, loginMember.memberId());
        } catch (IllegalStateException e) {
            model.addAttribute("form", claimService.prepareForm(token));
            model.addAttribute("error", e.getMessage());
            return "share/claim";
        }
        return "redirect:/me";   // 흡수 후 내 피드로
    }
}