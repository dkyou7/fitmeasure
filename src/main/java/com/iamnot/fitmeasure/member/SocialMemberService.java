package com.iamnot.fitmeasure.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소셜 provider 정보로 회원을 찾거나 만든다.
 * 웹 OAuth2 로그인과 앱 토큰 발급이 공유하는 진입점.
 */
@Service
@RequiredArgsConstructor
public class SocialMemberService {

    private final MemberRepository memberRepository;
    private final MemberCredentialRepository credentialRepository;

    @Transactional
    public Member findOrCreate(AuthProvider provider, String providerId) {
        return credentialRepository
                .findByProviderAndProviderId(provider, providerId)
                .map(MemberCredential::getMember)
                .orElseGet(() -> {
                    Member m = Member.create();
                    memberRepository.save(m);
                    credentialRepository.save(
                            MemberCredential.social(m, provider, providerId, null));
                    return m;
                });
    }
}