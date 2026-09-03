package com.iamnot.fitmeasure.config.security;

import com.iamnot.fitmeasure.member.AuthProvider;
import com.iamnot.fitmeasure.member.MemberCredential;
import com.iamnot.fitmeasure.member.MemberCredentialRepository;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginMemberService implements UserDetailsService {

    private final MemberCredentialRepository credentialRepository;
    private final MembershipRepository membershipRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        // 1. 휴대폰 인증수단 찾기
        MemberCredential cred = credentialRepository
                .findByProviderAndProviderId(AuthProvider.PHONE, phone)
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 번호입니다."));

        // 2. 그 Member의 로그인 가능한(OWNER/STAFF) 멤버십
        List<Membership> memberships = membershipRepository
                .findLoginableByMemberId(cred.getMember().getId());
        if (memberships.isEmpty()) {
            throw new UsernameNotFoundException("로그인 권한이 있는 소속이 없습니다.");
        }
        Membership chosen = memberships.stream()
                .filter(m -> m.getRole() == MembershipRole.OWNER)
                .findFirst()
                .orElse(memberships.get(0));

        return new LoginMember(chosen, phone, cred.getPasswordHash());
    }
}