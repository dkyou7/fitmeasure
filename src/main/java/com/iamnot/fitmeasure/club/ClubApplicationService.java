package com.iamnot.fitmeasure.club;

import com.iamnot.fitmeasure.admin.dto.ClubApplicationRow;
import com.iamnot.fitmeasure.measurement.template.MeasurementTemplateRepository;
import com.iamnot.fitmeasure.member.Member;
import com.iamnot.fitmeasure.member.MemberRepository;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubApplicationService {

    private final ClubRepository clubRepository;
    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final MeasurementTemplateRepository templateRepository;

    /** 사용자가 헬스장 오픈 신청 (PENDING 생성) */
    @Transactional
    public void apply(Long memberId, String name, ClubType type, String phone, String address) {
        if (clubRepository.existsByApplicantMemberIdAndStatus(memberId, ClubStatus.PENDING)) {
            throw new IllegalStateException("이미 처리 대기 중인 신청이 있어요.");
        }
        clubRepository.save(Club.apply(name.trim(), type, phone, address, memberId));
    }

    /** 대기 중 신청 목록 */
    @Transactional(readOnly = true)
    public List<ClubApplicationRow> listPending() {
        return clubRepository.findByStatus(ClubStatus.PENDING).stream()
                .map(c -> {
                    String applicantName = memberRepository.findById(c.getApplicantMemberId())
                            .map(Member::getName).orElse("(알 수 없음)");
                    return new ClubApplicationRow(
                            c.getId(), c.getName(), c.getType().name(),
                            c.getPhone(), c.getAddress(), applicantName,
                            c.getCreatedAt().toLocalDate());
                })
                .toList();
    }

    /** 승인: slug 생성 + APPROVED + 신청자 OWNER 연결 + 표준 프로그램 복사 */
    @Transactional
    public void approve(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없습니다."));

        club.approve(toSlug(club.getName()));

        Member owner = memberRepository.findById(club.getApplicantMemberId())
                .orElseThrow(() -> new IllegalStateException("신청자 계정을 찾을 수 없습니다."));
        String nickname = owner.getName() != null ? owner.getName() : "사장님";
        membershipRepository.save(new Membership(club, owner, MembershipRole.OWNER, nickname));

        templateRepository.findByClubIsNull()
                .forEach(std -> templateRepository.save(std.copyForClub(club)));
    }

    /** 거절 */
    @Transactional
    public void reject(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없습니다."));
        club.reject();
    }

    private String toSlug(String name) {
        String base = name.trim().toLowerCase()
                .replaceAll("[^a-z0-9가-힣]+", "-").replaceAll("(^-|-$)", "");
        if (base.isBlank()) base = "club";
        String slug = base; int n = 1;
        while (clubRepository.findBySlugAndStatus(slug, ClubStatus.APPROVED).isPresent())
            slug = base + "-" + (++n);
        return slug;
    }

    @Transactional(readOnly = true)
    public boolean hasPending(Long memberId) {
        return clubRepository.existsByApplicantMemberIdAndStatus(memberId, ClubStatus.PENDING);
    }
}