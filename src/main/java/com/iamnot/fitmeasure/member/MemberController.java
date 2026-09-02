package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.membership.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("members", memberService.listMembers());
        return "member/list";
    }

    /** htmx: 회원 등록 후 목록 프래그먼트만 갱신 */
    @PostMapping
    public String register(@RequestParam(required = false) String nickname,
                           @RequestParam(required = false) String memberNo,
                           Model model) {
        memberService.register(nickname, memberNo);
        model.addAttribute("members", memberService.listMembers());
        return "member/list :: memberTable";
    }
}