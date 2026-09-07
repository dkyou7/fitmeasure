package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.config.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MemberClaimController {

    private final MemberClaimService claimService;

    /** 로그인 후 리다이렉트로 진입 (GET) */
    @GetMapping("/s/{token}/absorb")
    public String absorbByRedirect(@PathVariable String token,
                                   @AuthenticationPrincipal LoginMember loginMember) {
        if (loginMember == null) {
            return "redirect:/login?next=/s/" + token + "/absorb";
        }
        tryAbsorb(token, loginMember);
        return "redirect:/me";
    }

    /** 로그인 상태에서 공유 카드 버튼으로 진입 (POST) */
    @PostMapping("/s/{token}/absorb")
    public String absorbBySubmit(@PathVariable String token,
                                 @AuthenticationPrincipal LoginMember loginMember) {
        if (loginMember == null) {
            return "redirect:/login?next=/s/" + token + "/absorb";
        }
        tryAbsorb(token, loginMember);
        return "redirect:/me";
    }

    private void tryAbsorb(String token, LoginMember loginMember) {
        try {
            claimService.absorbToLoggedIn(token, loginMember.memberId());
        } catch (IllegalStateException e) {
            // 이미 흡수됐거나 중복이면 무시하고 피드로 (재클릭·중복 대비)
        }
    }
}