package com.iamnot.fitmeasure.membership;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.config.security.AppPrincipal;
import com.iamnot.fitmeasure.member.Member;
import com.iamnot.fitmeasure.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ConnectService {

    private static final int EXPIRE_MINUTES = 5;

    private final CurrentClub currentClub;
    private final ClubRepository clubRepository;
    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final ConnectCodeRepository connectCodeRepository;

    /** 회원이 자기 연결 코드 발급 */
    @Transactional
    public String issueCode(AppPrincipal principal) {
        Member member = memberRepository.findById(principal.memberId()).orElseThrow();
        // 기존 미사용 코드 무효화 (재발급 시 옛 코드 정리)
        connectCodeRepository.markAllUsedByMemberId(member.getId());

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        connectCodeRepository.save(
                new ConnectCode(member, code, LocalDateTime.now().plusMinutes(EXPIRE_MINUTES)));
        return code;
    }

    @Transactional
    public Long connectByCode(String code) {
        ConnectCode cc = connectCodeRepository
                .findFirstByCodeAndUsedFalseOrderByCreatedAtDesc(code.trim())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 코드예요."));
        if (!cc.isValid()) {
            throw new IllegalStateException("만료된 코드예요. 회원에게 코드를 다시 요청하세요.");
        }

        Long clubId = currentClub.clubId();
        Member member = cc.getMember();

        if (membershipRepository.existsByClubIdAndMemberId(clubId, member.getId())) {
            throw new IllegalStateException("이미 등록된 회원이에요.");
        }

        // 무료 한도 체크 — 신규 연결 시점에만 (기존 회원은 grandfather로 유지)
        Club club = clubRepository.findById(clubId).orElseThrow();
        if (club.isFree()) {
            long current = membershipRepository.countByClubIdAndRole(clubId, MembershipRole.MEMBER);
            if (current >= club.getFreeMemberLimit()) {
                throw new IllegalStateException(
                        "무료 플랜은 회원 " + club.getFreeMemberLimit() + "명까지 연결할 수 있어요. " +
                                "더 받으시려면 유료 플랜으로 전환해주세요.");
            }
        }

        Club clubRef = clubRepository.getReferenceById(clubId);
        String nickname = member.getName() != null ? member.getName() : "회원";
        membershipRepository.save(new Membership(clubRef, member, MembershipRole.MEMBER, nickname));
        cc.markUsed();

        return member.getId();
    }
}