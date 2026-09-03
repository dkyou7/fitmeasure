package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.config.security.LoginMember;
import com.iamnot.fitmeasure.member.dto.ProfileView;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;

    @Transactional(readOnly = true)
    public ProfileView getProfile(LoginMember lm) {
        Member m = memberRepository.findById(lm.memberId()).orElseThrow();
        String nickname = null;
        boolean hasClub = lm.clubId() != null;
        if (hasClub) {
            nickname = membershipRepository
                    .findByMemberIdAndClubId(lm.memberId(), lm.clubId())
                    .map(Membership::getNickname).orElse(null);
        }
        return new ProfileView(m.getName(), nickname, m.getPhone(), hasClub);
    }

    /** 이름 + 현재 클럽 닉네임 수정 */
    @Transactional
    public void update(LoginMember lm, String name, String nickname) {
        Member m = memberRepository.findById(lm.memberId()).orElseThrow();
        m.updateName(name);

        // 현재 클럽에서의 닉네임도 수정 (클럽 소속이면)
        if (lm.clubId() != null && nickname != null && !nickname.isBlank()) {
            membershipRepository
                .findByMemberIdAndClubId(lm.memberId(), lm.clubId())
                .ifPresent(ms -> ms.rename(nickname.trim()));
        }
    }
}