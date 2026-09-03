package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.member.dto.ClaimForm;
import lombok.RequiredArgsConstructor;
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
}