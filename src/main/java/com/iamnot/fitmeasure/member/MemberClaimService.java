package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.measurement.session.MeasurementSession;
import com.iamnot.fitmeasure.measurement.session.MeasurementSessionRepository;
import com.iamnot.fitmeasure.member.dto.ClaimForm;
import com.iamnot.fitmeasure.membership.Membership;
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