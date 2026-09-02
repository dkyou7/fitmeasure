package com.iamnot.fitmeasure.membership;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.member.Member;
import com.iamnot.fitmeasure.member.MemberRepository;
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
    public List<Membership> listMembers() {
        return membershipRepository.findByClubIdAndRoleOrderByNicknameAsc(
                currentClub.clubId(), MembershipRole.MEMBER);
    }

    /** 트레이너가 회원 등록. nickname 비우면 자동 생성(익명) */
    @Transactional
    public Membership register(String nickname, String memberNo) {
        Club club = clubRepository.getReferenceById(currentClub.clubId());

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