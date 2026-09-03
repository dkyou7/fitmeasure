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

    @Transactional
    public Membership register(String phone, String memberNo) {
        Long clubId = currentClub.clubId();
        Club club = clubRepository.getReferenceById(clubId);

        // 전화번호 있으면 담고, 익명 회원 생성
        Member person = (phone != null && !phone.isBlank())
                ? Member.anonymousWithPhone(phone.trim())
                : Member.anonymous();
        memberRepository.save(person);

        String nickname = nicknameGenerator.generate();  // 이름은 항상 익명 자동생성
        Membership membership = new Membership(club, person, MembershipRole.MEMBER, nickname);
        if (memberNo != null && !memberNo.isBlank()) {
            membership.assignMemberNo(memberNo.trim());
        }
        return membershipRepository.save(membership);
    }
}