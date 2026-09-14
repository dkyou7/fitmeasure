package com.iamnot.fitmeasure.member.api;

import com.iamnot.fitmeasure.config.security.AppPrincipal;
import com.iamnot.fitmeasure.member.MemberFeedService;
import com.iamnot.fitmeasure.member.dto.ClubSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 회원 앱용 조회 API.
 * 현재는 세션 인증에 의존 — 앱 연동 전 토큰 인증 추가 필요.
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberFeedService feedService;

    @GetMapping("/records")
    public ResponseEntity<List<ClubSummary>> records(@AuthenticationPrincipal AppPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(feedService.myClubs(principal));
    }
}