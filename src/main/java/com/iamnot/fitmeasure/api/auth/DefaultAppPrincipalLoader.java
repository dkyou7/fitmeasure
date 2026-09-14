package com.iamnot.fitmeasure.api.auth;

import com.iamnot.fitmeasure.config.security.AppPrincipal;
import com.iamnot.fitmeasure.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DefaultAppPrincipalLoader implements AppPrincipalLoader {

    private final MemberRepository memberRepository;

    /**
     * 앱 API는 회원 전용이라 클럽 컨텍스트가 없다.
     * clubId를 쓰는 서비스는 API로 노출하지 않는다.
     */
    @Override
    @Transactional(readOnly = true)
    public AppPrincipal load(Long memberId) {
        return memberRepository.findById(memberId)
                .map(m -> (AppPrincipal) new ApiPrincipal(
                        m.getId(), m.getName(), m.isPlatformAdmin(), m.isOnboarded()))
                .orElse(null);
    }

    private record ApiPrincipal(Long memberId, String nickname,
                                boolean platformAdmin, boolean onboarded)
            implements AppPrincipal {

        @Override public boolean isPlatformAdmin() { return platformAdmin; }
        @Override public boolean isOnboarded() { return onboarded; }
        @Override public Long clubId() { return null; }
    }
}