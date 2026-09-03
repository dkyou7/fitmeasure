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
                        m.getMember().getPhone(),
                        !m.getMember().isClaimed(),
                        m.getJoinedAt()))
                .toList();
    }

    @Transactional
    public Membership register(String phone, boolean consent) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
        if (!consent) {
            throw new IllegalArgumentException("회원 동의 확인이 필요합니다.");
        }

        Long clubId = currentClub.clubId();
        Club club = clubRepository.getReferenceById(clubId);
        String normalizedPhone = phone.replaceAll("[^0-9]", "");

        Member person = memberRepository.findByPhone(normalizedPhone)
                .orElseGet(() -> memberRepository.save(Member.withPhone(normalizedPhone)));

        if (membershipRepository.existsByClubIdAndMemberId(clubId, person.getId())) {
            throw new IllegalStateException("이미 이 회원이 등록되어 있어요.");
        }

        String nickname = nicknameGenerator.generate();
        Membership membership = new Membership(club, person, MembershipRole.MEMBER, nickname);
        return membershipRepository.save(membership);
    }
}