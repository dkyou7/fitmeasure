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

    /** claim 제출 */
    @PostMapping("/s/{token}/claim")
    public String submit(@PathVariable String token,
                         @RequestParam String phone,
                         @RequestParam(required = false) String name,
                         Model model) {
        try {
            claimService.claim(token, phone.trim(), name);
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