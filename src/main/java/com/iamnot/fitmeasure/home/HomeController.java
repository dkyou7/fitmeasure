package com.iamnot.fitmeasure.home;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.config.security.AppPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ClubRepository clubRepository;
    private final HomeService homeService;

    @GetMapping("/")
    public String root(@AuthenticationPrincipal AppPrincipal principal, Model model) {
        if (principal == null) return "landing";
        if (principal.isPlatformAdmin()) return "redirect:/admin";
        if (principal.clubId() == null) return "redirect:/me";

        Club club = clubRepository.findById(principal.clubId()).orElse(null);
        if (club == null) return "redirect:/me";

        model.addAttribute("home", homeService.ownerHome(principal.clubId()));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}