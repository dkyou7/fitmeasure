package com.iamnot.fitmeasure.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberDetailService memberDetailService;  // 필드 추가

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
}