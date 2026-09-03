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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberCredential cred = credentialRepository
                .findByProviderAndProviderId(AuthProvider.USERNAME, username)
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 아이디입니다."));

        // 로그인 가능한 클럽 소속이 있으면 그걸로, 없으면 떠도는 계정
        List<Membership> memberships = membershipRepository
                .findLoginableByMemberId(cred.getMember().getId());

        Membership chosen = memberships.stream()
                .filter(m -> m.getRole() == MembershipRole.OWNER)
                .findFirst()
                .orElse(memberships.isEmpty() ? null : memberships.get(0));

        // 클럽 소속 여부와 무관하게 인증은 성공
        return new LoginMember(cred.getMember(), username, cred.getPasswordHash(), chosen);
    }
}