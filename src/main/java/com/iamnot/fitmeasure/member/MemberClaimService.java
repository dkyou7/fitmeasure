package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.measurement.session.MeasurementSession;
import com.iamnot.fitmeasure.measurement.session.MeasurementSessionRepository;
import com.iamnot.fitmeasure.member.dto.ClaimForm;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberClaimService {

    private final MeasurementSessionRepository sessionRepository;
    private final MemberCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final MembershipRepository membershipRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public ClaimForm prepareForm(String token) {
        MeasurementSession session = loadByToken(token);
        Membership m = session.getMembership();
        return new ClaimForm(token, m.getNickname(), m.getClub().getName());
    }

    @Transactional
    public void claimByUsername(String token, String username, String rawPassword) {
        MeasurementSession session = loadByToken(token);
        Membership membership = session.getMembership();
        Member member = membership.getMember();

        if (member.isClaimed()) {
            throw new IllegalStateException("이미 계정이 연결된 기록입니다.");
        }
        String id = username.trim();
        if (credentialRepository.findByProviderAndProviderId(AuthProvider.USERNAME, id).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 아이디예요.");
        }

        // 무료 한도 체크
        Club club = membership.getClub();
        if (club.isFree()) {
            long claimed = membershipRepository.countClaimedMembers(club.getId());
            if (claimed >= club.getFreeMemberLimit()) {
                throw new IllegalStateException("이 헬스장은 무료 회원 한도가 찼어요. 헬스장에 문의해주세요.");
            }
        }

        member.claim(membership.getNickname());  // 기존 닉네임 유지
        credentialRepository.save(
                MemberCredential.username(member, id, passwordEncoder.encode(rawPassword)));
    }

    private MeasurementSession loadByToken(String token) {
        return sessionRepository.findByShareTokenAndShareEnabledTrue(token)
                .orElseThrow(() -> new IllegalArgumentException("공유된 기록을 찾을 수 없습니다."));
    }

    /** 로그인한 사람이 이 세션(측정 기록)을 자기 계정에 흡수. 토큰이 인증. */
    @Transactional
    public void absorbToLoggedIn(String token, Long loginMemberId) {
        MeasurementSession session = loadByToken(token);
        Membership membership = session.getMembership();
        Member anonymousMember = membership.getMember();

        if (anonymousMember.isClaimed()) {
            throw new IllegalStateException("이미 계정이 연결된 기록입니다.");
        }
        Member loginMember = memberRepository.findById(loginMemberId).orElseThrow();

        // 이 익명 Membership의 소유자를 로그인한 사람으로 이전
        membership.transferTo(loginMember);
    }
}