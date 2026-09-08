package com.iamnot.fitmeasure.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SignupController {

    private final SignupService signupService;

    @GetMapping("/signup")
    public String form() {
        return "signup";
    }

    @PostMapping("/signup")
    public String submit(@RequestParam String username,
                         @RequestParam String password,
                         @RequestParam String passwordConfirm,
                         Model model) {
        try {
            signupService.signup(username, password, passwordConfirm);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
        return "redirect:/login?signup";
    }
}