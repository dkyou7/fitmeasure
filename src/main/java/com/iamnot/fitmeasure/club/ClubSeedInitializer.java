package com.iamnot.fitmeasure.club;

import com.iamnot.fitmeasure.measurement.session.MeasurementSession;
import com.iamnot.fitmeasure.measurement.session.MeasurementSessionRepository;
import com.iamnot.fitmeasure.measurement.session.MeasurementValue;
import com.iamnot.fitmeasure.measurement.session.MeasurementValueRepository;
import com.iamnot.fitmeasure.measurement.template.MeasurementTemplate;
import com.iamnot.fitmeasure.measurement.template.MeasurementTemplateRepository;
import com.iamnot.fitmeasure.measurement.template.TemplateItem;
import com.iamnot.fitmeasure.member.*;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 개발용 데모 클럽 시드. 표준 템플릿 시드 이후 실행(@Order).
 * 시드 계정은 온보딩 완료 상태로 만들어 로그인 시 온보딩을 거치지 않는다.
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class ClubSeedInitializer implements ApplicationRunner {

    private final ClubRepository clubRepository;
    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final MeasurementTemplateRepository templateRepository;
    private final MemberCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final MeasurementSessionRepository sessionRepository;
    private final MeasurementValueRepository valueRepository;

    private static final String ADMIN_ID = "dkyou7@ktnet.co.kr";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 운영자는 클럽과 무관하게 항상 보장 (없으면 생성)
        seedAdmin();
        if (clubRepository.findBySlugAndStatus("demo-gym", ClubStatus.APPROVED).isPresent()) {
            log.info("데모 클럽이 이미 존재하여 시드를 건너뜁니다.");
            return;
        }

        // 클럽
        Club club = clubRepository.save(new Club("데모 헬스장", "demo-gym", ClubType.GYM));
        club.updateListed(true);
        club.updateIntro("회원 한 분 한 분의 성장을 데이터로 관리합니다. " +
                "3대 측정과 기초 체력을 정기적으로 측정해 눈에 보이는 변화를 만들어드려요.");

        // 사장 / 트레이너 / 회원
        seedMember(club, MembershipRole.OWNER, "사장님", "123", "123");
        seedMember(club, MembershipRole.STAFF, "김트레이너", "234", "234");

        // 표준 프로그램 복사
        List<MeasurementTemplate> standards = templateRepository.findByClubIsNull();
        standards.forEach(std -> templateRepository.save(std.copyForClub(club)));
        log.info("표준 프로그램 {}개 복사 완료", standards.size());

        log.info("데모 클럽 시드 완료: {} (id={})", club.getName(), club.getId());
        seedDemoMeasurements(club);
    }

    private void seedAdmin() {
        if (credentialRepository.findByProviderAndProviderId(AuthProvider.USERNAME, ADMIN_ID).isPresent()) {
            return;
        }
        Member admin = Member.anonymous();
        admin.completeOnboarding("운영자", null);   // 이름 + 온보딩 완료
        admin.grantPlatformAdmin();
        memberRepository.save(admin);
        credentialRepository.save(MemberCredential.username(
                admin, ADMIN_ID, passwordEncoder.encode("1q2w3e!@")));
        log.info("운영자 시드 완료: {}", ADMIN_ID);
    }

    private void seedMember(Club club, MembershipRole role, String name,
                            String username, String rawPassword) {
        Member person = Member.anonymous();
        person.completeOnboarding(name, null);      // 이름 + 온보딩 완료
        memberRepository.save(person);
        credentialRepository.save(MemberCredential.username(
                person, username, passwordEncoder.encode(rawPassword)));
        membershipRepository.save(new Membership(club, person, role, name));
    }
    /** 시연용 측정 데이터 — 회원 몇 명에 여러 회차, 성장 그래프가 그려지게 */
    private void seedDemoMeasurements(Club club) {
        // 이 클럽의 "3대 측정" 프로그램과 항목
        MeasurementTemplate program = templateRepository.findByClubId(club.getId()).stream()
                .filter(t -> t.getName().contains("3대"))
                .findFirst().orElse(null);
        if (program == null) return;

        List<TemplateItem> items = program.getItems().stream()
                .filter(TemplateItem::isActive).toList();

        // 측정할 트레이너(측정자)
        Membership trainer = membershipRepository
                .findByClubIdAndRoleOrderByNicknameAsc(club.getId(), MembershipRole.STAFF)
                .stream().findFirst().orElse(null);

        // 데모 회원 3명 + 각자 성장 곡선 (시작값, 회차당 증가폭)
        seedMemberGrowth(club, program, items, trainer, "김성장",
                Map.of("벤치프레스", 70.0, "스쿼트", 90.0, "데드리프트", 100.0),
                Map.of("벤치프레스", 5.0, "스쿼트", 7.0, "데드리프트", 8.0));
        seedMemberGrowth(club, program, items, trainer, "박근육",
                Map.of("벤치프레스", 100.0, "스쿼트", 130.0, "데드리프트", 150.0),
                Map.of("벤치프레스", 3.0, "스쿼트", 5.0, "데드리프트", 5.0));
        seedMemberGrowth(club, program, items, trainer, "이초보",
                Map.of("벤치프레스", 40.0, "스쿼트", 50.0, "데드리프트", 60.0),
                Map.of("벤치프레스", 4.0, "스쿼트", 6.0, "데드리프트", 7.0));

        log.info("시연용 측정 데이터 시드 완료");
    }

    private void seedMemberGrowth(Club club, MeasurementTemplate program, List<TemplateItem> items,
                                  Membership trainer, String name,
                                  Map<String, Double> baseByItem, Map<String, Double> gainByItem) {
        Member person = Member.anonymous();
        person.completeOnboarding(name, null);
        memberRepository.save(person);
        Membership membership = membershipRepository.save(
                new Membership(club, person, MembershipRole.MEMBER, name));

        int rounds = 4;
        for (int r = 0; r < rounds; r++) {
            LocalDateTime measuredAt = LocalDateTime.now().minusDays(28L * (rounds - 1 - r));
            // 순서: membership, template, measuredBy, measuredAt
            MeasurementSession session = new MeasurementSession(membership, program, trainer, measuredAt);

            for (TemplateItem item : items) {
                String key = item.getName().replaceAll("\\s*1RM", "").trim();
                Double base = baseByItem.get(key);
                Double gain = gainByItem.get(key);
                if (base == null) continue;
                double value = base + gain * r;
                session.addValue(MeasurementValue.ofNumber(item, BigDecimal.valueOf(value)));
            }
            sessionRepository.save(session);   // cascade로 value도 저장
        }
    }
}