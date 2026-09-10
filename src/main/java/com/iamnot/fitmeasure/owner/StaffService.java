package com.iamnot.fitmeasure.owner;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubRepository;
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
    private final ClubRepository clubRepository;   // 주입 추가

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
    public void demote(Long membershipId) {
        find(membershipId).demoteToMember();
    }

    @Transactional
    public void connectStaffByCode(String code) {
        checkStaffLimit(currentClub.clubId());
        Long memberId = connectService.connectByCode(code);   // 순수 연결(회원한도 X)
        Membership m = membershipRepository
                .findByClubIdAndMemberId(currentClub.clubId(), memberId)
                .orElseThrow(() -> new IllegalStateException("연결에 실패했어요."));
        m.promoteToStaff();
    }

    @Transactional
    public void promote(Long membershipId) {
        checkStaffLimit(currentClub.clubId());
        find(membershipId).promoteToStaff();
    }

    private Membership find(Long membershipId) {
        return membershipRepository
                .findByIdAndClubId(membershipId, currentClub.clubId())
                .orElseThrow(() -> new IllegalArgumentException("대상을 찾을 수 없습니다."));
    }

    private void checkStaffLimit(Long clubId) {
        Club club = clubRepository.findById(clubId).orElseThrow();
        long currentStaff = membershipRepository.countByClubIdAndRole(clubId, MembershipRole.STAFF);
        int limit = club.getPlan().getStaffLimit();
        if (currentStaff >= limit) {
            throw new IllegalStateException(
                    club.getPlan().getLabel() + " 플랜은 트레이너 " + limit + "명까지예요." +
                            (club.isFree() ? " 유료 전환 시 3명까지 등록할 수 있어요." : ""));
        }
    }

}