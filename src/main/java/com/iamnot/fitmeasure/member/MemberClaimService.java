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

    @Transactional(readOnly = true)
    public ClaimForm prepareForm(String token) {
        MeasurementSession session = loadByToken(token);
        Membership m = session.getMembership();
        return new ClaimForm(token, m.getNickname(), m.getClub().getName());
    }

    /** 휴대폰으로 익명 회원을 계정으로 전환 */
    @Transactional
    public void claimByPhone(String token, String phone, String rawPassword, String name) {
        MeasurementSession session = loadByToken(token);
        Membership membership = session.getMembership();
        Member member = membership.getMember();

        if (member.isClaimed()) {
            throw new IllegalStateException("이미 계정이 연결된 기록입니다.");
        }
        if (credentialRepository.findByProviderAndProviderId(AuthProvider.PHONE, phone).isPresent()) {
            throw new IllegalStateException("이미 가입된 번호입니다. 계정 통합은 준비 중이에요.");
        }

        // 무료 한도 체크: 이 claim이 한도를 넘기는가
        Club club = membership.getClub();
        if (club.isFree()) {
            long claimed = membershipRepository.countClaimedMembers(club.getId());
            if (claimed >= club.getFreeMemberLimit()) {
                throw new IllegalStateException(
                        "이 헬스장은 무료 회원 한도가 찼어요. 헬스장에 문의해주세요.");
            }
        }

        member.claim(name);
        credentialRepository.save(
                MemberCredential.phone(member, phone, passwordEncoder.encode(rawPassword)));

        if (name != null && !name.isBlank()) {
            membership.rename(name.trim());
        }
    }

    private MeasurementSession loadByToken(String token) {
        return sessionRepository.findByShareTokenAndShareEnabledTrue(token)
                .orElseThrow(() -> new IllegalArgumentException("공유된 기록을 찾을 수 없습니다."));
    }
}