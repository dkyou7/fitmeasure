package com.iamnot.fitmeasure.admin;

import com.iamnot.fitmeasure.admin.dto.AdminClubRow;
import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ClubRepository clubRepository;
    private final MembershipRepository membershipRepository;

    @Transactional(readOnly = true)
    public List<AdminClubRow> listClubs() {
        return clubRepository.findAll().stream()
                .map(c -> new AdminClubRow(
                        c.getId(), c.getName(), c.getType().name(), c.getPlan().name(),
                        membershipRepository.countClaimedMembers(c.getId()),
                        membershipRepository.countByClubIdAndRole(c.getId(), MembershipRole.STAFF),
                        c.getCreatedAt().toLocalDate()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminStats stats() {
        List<Club> clubs = clubRepository.findAll();
        long paid = clubs.stream().filter(c -> !c.isFree()).count();
        return new AdminStats(clubs.size(), paid);
    }

    /** 플랜 수동 전환 (수기 계약) */
    @Transactional
    public void togglePlan(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("클럽을 찾을 수 없습니다."));
        if (club.isFree()) {
            club.upgradeToPaid();
        } else {
            club.downgradeToFree();
        }
    }

    public record AdminStats(int totalClubs, long paidClubs) {}
}