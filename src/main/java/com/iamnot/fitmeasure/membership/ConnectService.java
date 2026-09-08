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
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        connectCodeRepository.save(
                new ConnectCode(member, code, LocalDateTime.now().plusMinutes(EXPIRE_MINUTES)));
        return code;
    }

    /** 트레이너가 코드로 회원을 자기 클럽에 연결 */
    @Transactional
    public void connectByCode(String code) {
        ConnectCode cc = connectCodeRepository
                .findFirstByCodeAndUsedFalseOrderByCreatedAtDesc(code.trim())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 코드예요."));
        if (!cc.isValid()) {
            throw new IllegalStateException("만료된 코드예요. 회원에게 코드를 다시 요청하세요.");
        }

        Long clubId = currentClub.clubId();
        Member member = cc.getMember();

        // 이미 이 클럽 회원이면 막기
        if (membershipRepository.existsByClubIdAndMemberId(clubId, member.getId())) {
            throw new IllegalStateException("이미 등록된 회원이에요.");
        }

        Club club = clubRepository.getReferenceById(clubId);
        String nickname = member.getName() != null ? member.getName() : "회원";
        membershipRepository.save(new Membership(club, member, MembershipRole.MEMBER, nickname));
        cc.markUsed();
    }
}