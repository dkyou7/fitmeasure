package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.measurement.session.MeasurementSession;
import com.iamnot.fitmeasure.measurement.session.MeasurementSessionRepository;
import com.iamnot.fitmeasure.member.dto.ClaimForm;
import com.iamnot.fitmeasure.membership.Membership;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberClaimService {

    private final MeasurementSessionRepository sessionRepository;
    private final MemberRepository memberRepository;

    /** claim 폼에 보여줄 정보 (공개 토큰으로 접근) */
    @Transactional(readOnly = true)
    public ClaimForm prepareForm(String token) {
        MeasurementSession session = loadByToken(token);
        Membership m = session.getMembership();
        return new ClaimForm(token, m.getNickname(), m.getClub().getName());
    }

    /**
     * 익명 회원을 계정으로 전환.
     * 이미 그 전화번호로 가입된 사람이면 merge는 하지 않고 안내(2차 과제).
     */
    @Transactional
    public void claim(String token, String phone, String name) {
        MeasurementSession session = loadByToken(token);
        Membership membership = session.getMembership();
        Member member = membership.getMember();

        if (member.isClaimed()) {
            throw new IllegalStateException("이미 계정이 연결된 기록입니다.");
        }
        // 전화번호 중복 체크 (이미 다른 Member가 이 번호로 가입)
        if (memberRepository.findByPhone(phone).isPresent()) {
            throw new IllegalStateException(
                    "이미 가입된 번호입니다. 계정 통합은 준비 중이에요.");
        }

        // 비밀번호는 이번 MVP에서 생략(전화번호 기반 간편 등록).
        // 추후 인증 붙일 때 passwordHash 채움.
        member.claim(phone, null, (name == null || name.isBlank()) ? null : name.trim());

        // 표시명도 실명으로 갱신(입력했다면)
        if (name != null && !name.isBlank()) {
            membership.rename(name.trim());
        }
    }

    private MeasurementSession loadByToken(String token) {
        return sessionRepository.findByShareTokenAndShareEnabledTrue(token)
                .orElseThrow(() -> new IllegalArgumentException("공유된 기록을 찾을 수 없습니다."));
    }
}