package com.iamnot.fitmeasure.api.auth;

import com.iamnot.fitmeasure.member.AuthProvider;
import com.iamnot.fitmeasure.member.Member;
import com.iamnot.fitmeasure.member.SocialMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final SocialTokenVerifier socialTokenVerifier;
    private final SocialMemberService socialMemberService;
    private final JwtProvider jwtProvider;

    public record SocialLoginRequest(AuthProvider provider, String accessToken) {}
    public record TokenResponse(String token, boolean onboarded) {}

    @PostMapping("/social")
    public ResponseEntity<TokenResponse> social(@RequestBody SocialLoginRequest req) {
        String providerId;
        try {
            providerId = socialTokenVerifier.verifyAndGetProviderId(req.provider(), req.accessToken());
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }

        Member member = socialMemberService.findOrCreate(req.provider(), providerId);
        return ResponseEntity.ok(
                new TokenResponse(jwtProvider.issue(member.getId()), member.isOnboarded()));
    }
}