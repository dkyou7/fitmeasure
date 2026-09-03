package com.iamnot.fitmeasure.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final MemberRepository memberRepository;
    private final MemberCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    /** 아이디+비번으로 신규 계정 생성 (클럽/역할 없음 — 떠도는 사람) */
    @Transactional
    public void signup(String username, String rawPassword, String name) {
        String id = username.trim();
        if (credentialRepository.findByProviderAndProviderId(AuthProvider.USERNAME, id).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 아이디예요.");
        }

        Member member = Member.anonymous();
        member.claim(name);   // claimedAt 찍힘, 이름 설정(있으면)
        memberRepository.save(member);

        credentialRepository.save(
                MemberCredential.username(member, id, passwordEncoder.encode(rawPassword)));
    }
}