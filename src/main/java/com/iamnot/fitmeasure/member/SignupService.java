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

    @Transactional
    public Long signup(String username, String rawPassword, String passwordConfirm) {
        if (!rawPassword.equals(passwordConfirm)) {
            throw new IllegalStateException("비밀번호가 일치하지 않아요.");
        }
        String id = username.trim();
        if (credentialRepository.findByProviderAndProviderId(AuthProvider.USERNAME, id).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 아이디예요.");
        }
        Member member = Member.anonymous();   // 이름 안 받음, onboardedAt null
        memberRepository.save(member);
        credentialRepository.save(
                MemberCredential.username(member, id, passwordEncoder.encode(rawPassword)));
        return member.getId();
    }
}