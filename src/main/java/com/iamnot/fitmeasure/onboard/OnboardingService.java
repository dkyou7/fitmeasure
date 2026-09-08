package com.iamnot.fitmeasure.onboard;

import com.iamnot.fitmeasure.member.Member;
import com.iamnot.fitmeasure.member.MemberRepository;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {
    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public void complete(Long memberId, String name, String phone) {
        Member m = memberRepository.findById(memberId).orElseThrow();
        m.completeOnboarding(name, phone);
        // 이름을 클럽 닉네임에도 동기화 (완전 동기화 원칙)
        if (name != null && !name.isBlank()) {
            membershipRepository.findByMemberIdAndRole(memberId, MembershipRole.MEMBER)
                    .forEach(ms -> ms.rename(name.trim()));
        }
    }
}