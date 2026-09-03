package com.iamnot.fitmeasure.config.security;

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

    private final MembershipRepository membershipRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        // 휴대폰으로 OWNER/STAFF 멤버십을 찾는다 (로그인 가능한 사람)
        List<Membership> memberships = membershipRepository.findLoginableByPhone(phone);
        if (memberships.isEmpty()) {
            throw new UsernameNotFoundException("가입되지 않은 번호입니다.");
        }
        // 한 사람이 여러 클럽 소속일 수 있으나, MVP는 첫 클럽으로.
        // (클럽 선택은 추후. 지금은 OWNER 우선, 없으면 첫 STAFF)
        Membership chosen = memberships.stream()
                .filter(m -> m.getRole() == MembershipRole.OWNER)
                .findFirst()
                .orElse(memberships.get(0));
        return new LoginMember(chosen);
    }
}