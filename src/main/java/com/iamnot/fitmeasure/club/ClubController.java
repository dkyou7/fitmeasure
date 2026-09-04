package com.iamnot.fitmeasure.club;

import com.iamnot.fitmeasure.config.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ClubController {

    private final ClubCreateService clubCreateService;

    @PostMapping("/clubs")
    public String create(@AuthenticationPrincipal LoginMember loginMember,
                         @RequestParam String name,
                         @RequestParam ClubType type) {
        clubCreateService.create(loginMember, name, type);
        // 클럽 생성 후 재로그인 없이는 세션의 clubId가 갱신 안 됨 → 재로그인 유도
        return "redirect:/clubs/created";
    }

    @GetMapping("/clubs/created")
    public String created() {
        return "club/created";
    }
}