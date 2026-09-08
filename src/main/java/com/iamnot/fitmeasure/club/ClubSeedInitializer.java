package com.iamnot.fitmeasure.club;

import com.iamnot.fitmeasure.measurement.template.MeasurementTemplate;
import com.iamnot.fitmeasure.measurement.template.MeasurementTemplateRepository;
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

import java.util.List;

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

    private static final String ADMIN_ID = "dkyou7@ktnet.co.kr";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 운영자는 클럽과 무관하게 항상 보장 (없으면 생성)
        seedAdmin();

        if (clubRepository.findBySlug("demo-gym").isPresent()) {
            log.info("데모 클럽이 이미 존재하여 시드를 건너뜁니다.");
            return;
        }

        // 클럽
        Club club = clubRepository.save(new Club("데모 헬스장", "demo-gym", ClubType.GYM));

        // 사장 / 트레이너 / 회원
        seedMember(club, MembershipRole.OWNER, "사장님", "123", "123");
        seedMember(club, MembershipRole.STAFF, "김트레이너", "234", "234");
        seedMember(club, MembershipRole.MEMBER, "유테스터", "345", "345");

        // 표준 프로그램 복사
        List<MeasurementTemplate> standards = templateRepository.findByClubIsNull();
        standards.forEach(std -> templateRepository.save(std.copyForClub(club)));
        log.info("표준 프로그램 {}개 복사 완료", standards.size());

        log.info("데모 클럽 시드 완료: {} (id={})", club.getName(), club.getId());
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
}