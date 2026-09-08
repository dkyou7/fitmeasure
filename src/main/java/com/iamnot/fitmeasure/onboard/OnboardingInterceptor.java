package com.iamnot.fitmeasure.onboard;

import com.iamnot.fitmeasure.config.security.AppPrincipal;
import com.iamnot.fitmeasure.member.Member;
import com.iamnot.fitmeasure.member.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/** 로그인했으나 온보딩 미완인 사용자를 /onboarding으로 유도한다. */
@Component
@RequiredArgsConstructor
public class OnboardingInterceptor implements HandlerInterceptor {

    private final MemberRepository memberRepository;
    private static final Set<String> ALLOWED = Set.of("/onboarding", "/logout", "/error");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (ALLOWED.contains(request.getRequestURI())) return true;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppPrincipal principal)) return true;

        // DB에서 실제 온보딩 여부 확인 (세션값 아님 → 완료 즉시 반영)
        boolean onboarded = memberRepository.findById(principal.memberId())
                .map(Member::isOnboarded).orElse(true);
        if (!onboarded) {
            response.sendRedirect("/onboarding");
            return false;
        }
        return true;
    }
}