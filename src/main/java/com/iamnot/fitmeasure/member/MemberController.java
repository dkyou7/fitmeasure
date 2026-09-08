package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.config.security.AppPrincipal;
import com.iamnot.fitmeasure.membership.ConnectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberDetailService memberDetailService;
    private final ConnectService connectService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("members", memberService.listMembers());
        return "member/list";
    }

    @PostMapping
    public String register(@RequestParam String phone,
                           @RequestParam(defaultValue = "false") boolean consent,
                           Model model) {
        try {
            memberService.register(phone, consent);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("regError", e.getMessage());
        }
        model.addAttribute("members", memberService.listMembers());
        return "member/list :: memberTable";
    }

    @GetMapping("/{membershipId}")
    public String detail(@PathVariable Long membershipId, Model model) {
        model.addAttribute("detail", memberDetailService.getDetail(membershipId));
        return "member/detail";
    }

    @GetMapping("/members/connect")
    public String connectForm() {
        return "member/connect";
    }

    @PostMapping("/members/connect")
    public String connect(@RequestParam String code, Model model) {
        try {
            connectService.connectByCode(code);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "member/connect";
        }
        return "redirect:/members";
    }

    @GetMapping("/connect")
    public String connectByQr(@RequestParam String code,
                              @AuthenticationPrincipal AppPrincipal principal, Model model) {
        if (principal == null) return "redirect:/login?next=/connect?code=" + code;
        if (principal.clubId() == null) {
            model.addAttribute("error", "헬스장 계정으로 로그인해주세요.");
            return "member/connect";
        }
        try {
            connectService.connectByCode(code);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "member/connect";
        }
        return "redirect:/members";
    }
}