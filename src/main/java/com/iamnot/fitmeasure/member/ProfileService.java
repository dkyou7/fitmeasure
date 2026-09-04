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
    private final MemberCredentialRepository credentialRepository;

    @Transactional(readOnly = true)
    public ProfileView getProfile(LoginMember lm) {
        Member m = memberRepository.findById(lm.memberId()).orElseThrow();

        // USERNAME credential에서 아이디
        String username = credentialRepository
                .findByMemberIdAndProvider(m.getId(), AuthProvider.USERNAME)
                .map(MemberCredential::getProviderId)
                .orElse(null);

        String nickname = null;
        boolean hasClub = lm.clubId() != null;
        if (hasClub) {
            nickname = membershipRepository
                    .findByMemberIdAndClubId(m.getId(), lm.clubId())
                    .map(Membership::getNickname).orElse(null);
        }

        return new ProfileView(
                username, m.getName(), nickname,
                m.getPhone(),                          // Member.phone (본인 번호)
                hasClub,
                m.getPhone() != null && !m.getPhone().isBlank());
    }

    /** 이름 + 현재 클럽 닉네임 수정 */
    @Transactional
    public void update(LoginMember lm, String name, String nickname, String phone) {
        Member m = memberRepository.findById(lm.memberId()).orElseThrow();
        m.updateName(name);
        m.updatePhone(phone == null ? null : phone.replaceAll("[^0-9]", ""));  // 본인 번호 수정

        if (lm.clubId() != null && nickname != null && !nickname.isBlank()) {
            membershipRepository.findByMemberIdAndClubId(lm.memberId(), lm.clubId())
                    .ifPresent(ms -> ms.rename(nickname.trim()));
        }
    }
}