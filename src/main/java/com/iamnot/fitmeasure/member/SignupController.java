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
                         @RequestParam(required = false) String name,
                         Model model) {
        try {
            signupService.signup(username, password, name);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
        // 가입 성공 → 로그인 화면으로 (가입했으니 로그인하라)
        return "redirect:/login?signup";
    }
}