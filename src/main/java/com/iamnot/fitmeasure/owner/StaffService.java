package com.iamnot.fitmeasure.owner;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.member.Member;
import com.iamnot.fitmeasure.member.MemberRepository;
import com.iamnot.fitmeasure.membership.*;
import com.iamnot.fitmeasure.owner.dto.StaffRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final CurrentClub currentClub;
    private final ClubRepository clubRepository;
    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;

    /** 운영진 목록(OWNER + STAFF) */
    @Transactional(readOnly = true)
    public List<StaffRow> listStaff() {
        Long clubId = currentClub.clubId();
        return Stream.concat(
                membershipRepository.findByClubIdAndRoleOrderByNicknameAsc(clubId, MembershipRole.OWNER).stream(),
                membershipRepository.findByClubIdAndRoleOrderByNicknameAsc(clubId, MembershipRole.STAFF).stream())
            .map(m -> new StaffRow(
                    m.getId(), m.getNickname(), m.getRole(), m.getStatus(),
                    m.getRole() == MembershipRole.OWNER, m.getJoinedAt()))
            .toList();
    }

    /** 트레이너 추가 (계정 없는 STAFF — 나중에 본인이 claim). 이름만 등록 */
    @Transactional
    public void addStaff(String nickname) {
        Long clubId = currentClub.clubId();
        Club club = clubRepository.getReferenceById(clubId);
        Member person = memberRepository.save(Member.anonymous());
        String name = (nickname == null || nickname.isBlank()) ? "새 트레이너" : nickname.trim();
        membershipRepository.save(new Membership(club, person, MembershipRole.STAFF, name));
    }

    /** 트레이너 활성/비활성 토글 */
    @Transactional
    public void toggleStaff(Long membershipId) {
        Membership m = membershipRepository
                .findByIdAndClubId(membershipId, currentClub.clubId())
                .orElseThrow(() -> new IllegalArgumentException("대상을 찾을 수 없습니다."));
        if (m.getRole() == MembershipRole.OWNER) {
            throw new IllegalStateException("사장 계정은 상태를 변경할 수 없습니다.");
        }
        if (m.getStatus() == MembershipStatus.ACTIVE) {
            m.deactivate();
        } else {
            m.activate();
        }
    }
}