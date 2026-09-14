package com.iamnot.fitmeasure.api.auth;

import com.iamnot.fitmeasure.member.AuthProvider;
import com.iamnot.fitmeasure.member.Member;
import com.iamnot.fitmeasure.member.SocialMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final KakaoAuthClient kakaoAuthClient;
    private final SocialMemberService socialMemberService;
    private final JwtProvider jwtProvider;

    /** 앱에서 받은 인가 코드. redirectUri는 인가 요청 때 쓴 값과 동일해야 한다. */
    public record KakaoLoginRequest(String code, String redirectUri) {}
    public record TokenResponse(String token, boolean onboarded) {}

    @PostMapping("/kakao")
    public ResponseEntity<TokenResponse> kakao(@RequestBody KakaoLoginRequest req) {
        String providerId;
        try {
            providerId = kakaoAuthClient.exchangeAndGetProviderId(req.code(), req.redirectUri());
        } catch (Exception e) {
            log.warn("카카오 인가 코드 교환 실패", e);
            return ResponseEntity.status(401).build();
        }

        Member member = socialMemberService.findOrCreate(AuthProvider.KAKAO, providerId);
        return ResponseEntity.ok(
                new TokenResponse(jwtProvider.issue(member.getId()), member.isOnboarded()));
    }
}