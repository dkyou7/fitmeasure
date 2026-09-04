package com.iamnot.fitmeasure.admin;

import com.iamnot.fitmeasure.admin.dto.AdminClubRow;
import com.iamnot.fitmeasure.admin.dto.AdminStats;
import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.club.ClubType;
import com.iamnot.fitmeasure.measurement.template.MeasurementTemplateRepository;
import com.iamnot.fitmeasure.member.*;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ClubRepository clubRepository;
    private final MembershipRepository membershipRepository;
    private final MemberRepository memberRepository;
    private final MemberCredentialRepository credentialRepository;
    private final MeasurementTemplateRepository templateRepository;
    private final PasswordEncoder passwordEncoder;

    /** 운영자가 헬스장 + 사장 계정을 생성 (계약 온보딩) */
    @Transactional
    public void createClubWithOwner(String clubName, ClubType type,
                                    String ownerName, String ownerUsername, String ownerPassword) {
        // 아이디 중복 체크
        if (credentialRepository.findByProviderAndProviderId(
                AuthProvider.USERNAME, ownerUsername.trim()).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 아이디예요.");
        }

        // 클럽
        String slug = toSlug(clubName);
        Club club = clubRepository.save(new Club(clubName.trim(), slug, type));

        // 사장 계정
        Member owner = Member.anonymous();
        owner.claim(ownerName);
        memberRepository.save(owner);
        credentialRepository.save(MemberCredential.username(
                owner, ownerUsername.trim(), passwordEncoder.encode(ownerPassword)));
        membershipRepository.save(new Membership(club, owner, MembershipRole.OWNER, ownerName));

        // 표준 프로그램 복사
        templateRepository.findByClubIsNull()
                .forEach(std -> templateRepository.save(std.copyForClub(club)));
    }

    private String toSlug(String name) {
        String base = name.trim().toLowerCase()
                .replaceAll("[^a-z0-9가-힣]+", "-").replaceAll("(^-|-$)", "");
        if (base.isBlank()) base = "club";
        String slug = base; int n = 1;
        while (clubRepository.findBySlug(slug).isPresent()) slug = base + "-" + (++n);
        return slug;
    }

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

    /** 아이디로 사용자 조회 (검색용) */
    @Transactional(readOnly = true)
    public MemberSearchResult findByUsername(String username) {
        MemberCredential cred = credentialRepository
                .findByProviderAndProviderId(AuthProvider.USERNAME, username.trim())
                .orElse(null);
        if (cred == null) return null;
        Member m = cred.getMember();
        return new MemberSearchResult(m.getId(), username.trim(), m.getName());
    }

    public record MemberSearchResult(Long memberId, String username, String name) {}

    /** 기존 계정을 OWNER로 하는 클럽 생성 (계약 온보딩) */
    @Transactional
    public void createClubForExistingMember(Long memberId, String clubName, ClubType type) {
        Member owner = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));

        String slug = toSlug(clubName);
        Club club = clubRepository.save(new Club(clubName.trim(), slug, type));

        String nickname = owner.getName() != null ? owner.getName() : "사장님";
        membershipRepository.save(new Membership(club, owner, MembershipRole.OWNER, nickname));

        templateRepository.findByClubIsNull()
                .forEach(std -> templateRepository.save(std.copyForClub(club)));
    }
}