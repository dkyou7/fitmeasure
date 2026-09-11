package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.measurement.session.MeasurementSessionRepository;
import com.iamnot.fitmeasure.member.dto.MemberRow;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final CurrentClub currentClub;
    private final MembershipRepository membershipRepository;
    private final MeasurementSessionRepository sessionRepository;

    /** 현재 클럽의 회원(MEMBER 역할) 목록 */
    @Transactional(readOnly = true)
    public List<MemberRow> listMembers() {
        return membershipRepository
                .findByClubIdAndRoleOrderByNicknameAsc(currentClub.clubId(), MembershipRole.MEMBER)
                .stream()
                .map(m -> {
                    LocalDate last = sessionRepository
                            .findFirstByMembershipIdOrderByMeasuredAtDesc(m.getId())
                            .map(s -> s.getMeasuredAt().toLocalDate())
                            .orElse(null);
                    return new MemberRow(m.getId(), m.getNickname(), last, m.getJoinedAt());
                })
                .toList();
    }
}