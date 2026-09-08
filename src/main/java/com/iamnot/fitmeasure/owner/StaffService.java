package com.iamnot.fitmeasure.owner;

import com.iamnot.fitmeasure.config.CurrentClub;
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
    public void connectStaffByCode(String code) {
        Long memberId = connectService.connectByCode(code);
        Membership m = membershipRepository
                .findByClubIdAndMemberId(currentClub.clubId(), memberId)
                .orElseThrow(() -> new IllegalStateException("연결에 실패했어요."));
        m.promoteToStaff();
    }

    @Transactional
    public void promote(Long membershipId) {
        find(membershipId).promoteToStaff();
    }

    @Transactional
    public void demote(Long membershipId) {
        find(membershipId).demoteToMember();
    }

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