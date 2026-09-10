package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.config.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 인증 수단(휴대폰/카카오/애플/구글)은 MemberCredential이 이 Member를
 * 단방향으로 참조한다. 관계 탐색은 MemberCredentialRepository로 한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    private Short birthYear;

    @Column(nullable = false)
    private boolean platformAdmin = false;

    private LocalDateTime onboardedAt;

    public boolean isOnboarded() { return onboardedAt != null; }

    public void completeOnboarding(String name, String phone) {
        this.name = (name == null || name.isBlank()) ? this.name : name.trim();
        this.phone = (phone == null || phone.isBlank()) ? null : phone.replaceAll("[^0-9]", "");
        this.onboardedAt = java.time.LocalDateTime.now();
    }

    public void grantPlatformAdmin() { this.platformAdmin = true; }

    public void revokePlatformAdmin() { this.platformAdmin = false; }

    public void updatePhone(String phone) {
        this.phone = (phone == null || phone.isBlank()) ? null : phone;
    }

    public static Member create() { return new Member();}

    public void updateName(String name) {
        this.name = (name == null || name.isBlank()) ? null : name.trim();
    }
}