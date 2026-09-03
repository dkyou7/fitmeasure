package com.iamnot.fitmeasure.club;

import com.iamnot.fitmeasure.config.security.LoginMember;
import com.iamnot.fitmeasure.measurement.template.MeasurementTemplate;
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
public class ClubCreateService {

    private final ClubRepository clubRepository;
    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final MeasurementTemplateRepository templateRepository;

    /** 로그인한 사람이 클럽을 만들고 그 클럽의 OWNER가 된다 */
    @Transactional
    public Long create(LoginMember loginMember, String name, ClubType type) {
        Member owner = memberRepository.getReferenceById(loginMember.memberId());

        // slug 자동 생성 (중복 방지)
        String slug = toSlug(name);

        Club club = clubRepository.save(new Club(name.trim(), slug, type));

        String nickname = loginMember.nickname() != null ? loginMember.nickname() : "사장님";
        membershipRepository.save(new Membership(club, owner, MembershipRole.OWNER, nickname));

        // 표준 프로그램 복사
        List<MeasurementTemplate> standards = templateRepository.findByClubIsNull();
        for (MeasurementTemplate std : standards) {
            templateRepository.save(std.copyForClub(club));
        }
        return club.getId();
    }

    private String toSlug(String name) {
        String base = name.trim().toLowerCase()
                .replaceAll("[^a-z0-9가-힣]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isBlank()) base = "club";
        String slug = base;
        int n = 1;
        while (clubRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + (++n);
        }
        return slug;
    }
}