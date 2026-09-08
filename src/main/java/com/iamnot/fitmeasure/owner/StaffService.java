package com.iamnot.fitmeasure.owner;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.member.Member;
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
    private final MembershipRepository membershipRepository;
    private final ConnectService connectService;
    private final ClubRepository clubRepository;
    private final ConnectCodeRepository connectCodeRepository;

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

    @Transactional
    public Long connectByCode(String code) {   // void → Long
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

        Club club = clubRepository.getReferenceById(clubId);
        String nickname = member.getName() != null ? member.getName() : "회원";
        membershipRepository.save(new Membership(club, member, MembershipRole.MEMBER, nickname));
        cc.markUsed();
        return member.getId();   // 추가
    }

    /** 회원 → 트레이너 승격 */
    @Transactional
    public void promote(Long membershipId) {
        find(membershipId).promoteToStaff();
    }

    /** 트레이너 → 회원 강등 */
    @Transactional
    public void demote(Long membershipId) {
        find(membershipId).demoteToMember();
    }

    /** 트레이너 활성/비활성 토글 */
    @Transactional
    public void toggleStaff(Long membershipId) {
        Membership m = find(membershipId);
        if (m.getRole() == MembershipRole.OWNER) {
            throw new IllegalStateException("사장 계정은 상태를 변경할 수 없습니다.");
        }
        if (m.getStatus() == MembershipStatus.ACTIVE) {
            m.deactivate();
        } else {
            m.activate();
        }
    }

    private Membership find(Long membershipId) {
        return membershipRepository
                .findByIdAndClubId(membershipId, currentClub.clubId())
                .orElseThrow(() -> new IllegalArgumentException("대상을 찾을 수 없습니다."));
    }
}