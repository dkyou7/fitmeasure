package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.config.security.AppPrincipal;
import com.iamnot.fitmeasure.config.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/me/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public String view(@AuthenticationPrincipal AppPrincipal principal, Model model) {
        if (principal == null) return "redirect:/login";
        model.addAttribute("profile", profileService.getProfile(principal));
        model.addAttribute("activeTab", "profile");
        return "member/profile";
    }

    @PostMapping
    public String update(@AuthenticationPrincipal AppPrincipal principal,
                         @RequestParam(required = false) String name,
                         @RequestParam(required = false) String phone) {
        profileService.update(principal, name, phone);
        return "redirect:/me/profile?saved";
    }
}