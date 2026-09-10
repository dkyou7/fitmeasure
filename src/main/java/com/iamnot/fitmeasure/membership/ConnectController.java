package com.iamnot.fitmeasure.membership;

import com.iamnot.fitmeasure.config.security.AppPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ConnectController {

    private final ConnectService connectService;

    /** 트레이너: 코드 입력 화면 */
    @GetMapping("/members/connect")
    public String connectForm() {
        return "member/connect";
    }

    /** 트레이너: 코드 입력 제출 (회원 연결) */
    @PostMapping("/members/connect")
    public String connect(@RequestParam String code, Model model) {
        try {
            connectService.connectMemberByCode(code);   // 회원 한도 체크 포함
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "member/connect";
        }
        return "redirect:/members";
    }

    /** QR 스캔으로 진입 (회원 연결) */
    @GetMapping("/connect")
    public String connectByQr(@RequestParam(required = false) String code,
                              @AuthenticationPrincipal AppPrincipal principal, Model model) {
        if (code == null || code.isBlank()) {
            model.addAttribute("error", "잘못된 접근이에요. 코드를 다시 확인해주세요.");
            return "member/connect";
        }
        if (principal == null) {
            String next = java.net.URLEncoder.encode("/connect?code=" + code,
                    java.nio.charset.StandardCharsets.UTF_8);
            return "redirect:/login?next=" + next;
        }
        if (principal.clubId() == null) {
            model.addAttribute("error", "헬스장 계정으로 로그인해주세요.");
            return "member/connect";
        }
        try {
            connectService.connectMemberByCode(code);   // 회원 한도 체크 포함
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "member/connect";
        }
        return "redirect:/members";
    }
}