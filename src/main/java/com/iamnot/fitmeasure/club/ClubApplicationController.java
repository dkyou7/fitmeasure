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

    @GetMapping
    public String applyForm(Model model) {
        model.addAttribute("types", ClubType.values());
        return "club-apply";
    }

    @PostMapping
    public String submit(@AuthenticationPrincipal AppPrincipal principal,
                         @RequestParam String name,
                         @RequestParam ClubType type,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String address) {
        applicationService.apply(principal.memberId(), name, type, phone, address);
        return "redirect:/apply/done";
    }

    @GetMapping("/done")
    public String done() {
        return "club-apply-done";
    }
}