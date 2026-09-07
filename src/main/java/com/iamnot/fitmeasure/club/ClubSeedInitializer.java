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
 * 개발용 데모 클럽 시드. 표준 템플릿 시드 이후 실행되어야 하므로 Order 지정.
 * 데모 클럽 생성 시 표준 템플릿을 복사해 클럽 기본 측정표로 붙인다.
 */
@Slf4j
@Component
@Order(2)  // StandardTemplateInitializer(기본 0)보다 뒤에 실행
@RequiredArgsConstructor
public class ClubSeedInitializer implements ApplicationRunner {

    private final ClubRepository clubRepository;
    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final MeasurementTemplateRepository templateRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberCredentialRepository credentialRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (clubRepository.findBySlug("demo-gym").isPresent()) {
            log.info("데모 클럽이 이미 존재하여 시드를 건너뜁니다.");
            return;
        }

        // 1. 클럽
        Club club = clubRepository.save(new Club("데모 헬스장", "demo-gym", ClubType.GYM));

        // 플랫폼 운영자 (클럽 없음)
        if (memberRepository.count() == 0 || credentialRepository
                .findByProviderAndProviderId(AuthProvider.USERNAME, "admin").isEmpty()) {
            Member admin = Member.anonymous();
            admin.claim("운영자");
            admin.grantPlatformAdmin();
            memberRepository.save(admin);
            credentialRepository.save(MemberCredential.username(
                    admin, "dkyou7@ktnet.co.kr", passwordEncoder.encode("1q2w3e!@")));
        }

        // 오너
        Member ownerPerson = Member.anonymous();
        ownerPerson.claim("사장님");
        memberRepository.save(ownerPerson);
        credentialRepository.save(MemberCredential.username(
                ownerPerson, "123", passwordEncoder.encode("123")));
        membershipRepository.save(new Membership(club, ownerPerson, MembershipRole.OWNER, "사장님"));

        // 트레이너
        Member trainerPerson = Member.anonymous();
        trainerPerson.claim("김트레이너");
        memberRepository.save(trainerPerson);
        credentialRepository.save(MemberCredential.username(
                trainerPerson, "234", passwordEncoder.encode("234")));
        membershipRepository.save(new Membership(club, trainerPerson, MembershipRole.STAFF, "김트레이너"));

        // 회원
        Member testPerson = Member.anonymous();
        testPerson.claim("유테스터");
        memberRepository.save(testPerson);
        credentialRepository.save(MemberCredential.username(
                testPerson, "345", passwordEncoder.encode("345")));
        membershipRepository.save(new Membership(club, testPerson, MembershipRole.MEMBER, "유테스터"));

        // 3. 표준 프로그램 전체를 클럽으로 복사
        List<MeasurementTemplate> standards = templateRepository.findByClubIsNull();
        for (MeasurementTemplate std : standards) {
            templateRepository.save(std.copyForClub(club));
        }
        log.info("표준 프로그램 {}개 복사 완료", standards.size());

        log.info("데모 클럽 시드 완료: {} (id={})", club.getName(), club.getId());
    }
}