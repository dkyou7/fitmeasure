package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.member.dto.MemberRow;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import com.iamnot.fitmeasure.membership.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final CurrentClub currentClub;
    private final ClubRepository clubRepository;
    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final NicknameGenerator nicknameGenerator;

    /** 현재 클럽의 회원(MEMBER 역할) 목록 */
    @Transactional(readOnly = true)
    public List<MemberRow> listMembers() {
        return membershipRepository
                .findByClubIdAndRoleOrderByNicknameAsc(currentClub.clubId(), MembershipRole.MEMBER)
                .stream()
                .map(m -> new MemberRow(
                        m.getId(),
                        m.getNickname(),
                        m.getMemberNo(),
                        !m.getMember().isClaimed(),   // 트랜잭션 안에서 접근 → OK
                        m.getJoinedAt()))
                .toList();
    }

    /** 트레이너가 회원 등록. nickname 비우면 자동 생성(익명) */
    @Transactional
    public Membership register(String nickname, String memberNo) {
        Long clubId = currentClub.clubId();
        Club club = clubRepository.getReferenceById(clubId);

        // 무료 한도 체크 (FREE 플랜일 때만)
        var clubEntity = clubRepository.findById(clubId).orElseThrow();
        if (clubEntity.isFree()) {
            long current = membershipRepository.countByClubIdAndRole(clubId, MembershipRole.MEMBER);
            if (current >= clubEntity.getFreeMemberLimit()) {
                throw new FreeLimitExceededException(clubEntity.getFreeMemberLimit());
            }
        }

        Member person = memberRepository.save(Member.anonymous());
        String name = (nickname == null || nickname.isBlank())
                ? nicknameGenerator.generate()
                : nickname.trim();
        Membership membership = new Membership(club, person, MembershipRole.MEMBER, name);
        if (memberNo != null && !memberNo.isBlank()) {
            membership.assignMemberNo(memberNo.trim());
        }
        return membershipRepository.save(membership);
    }
}