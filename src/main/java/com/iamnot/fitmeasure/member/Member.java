package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.config.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 자연인. claimedAt이 null이면 클럽이 만들어둔 익명 회원,
 * 채워지면 본인이 계정으로 전환(claim)한 상태다.
 * 인증 수단(휴대폰/카카오/애플/구글)은 MemberCredential이 이 Member를
 * 단방향으로 참조한다. 관계 탐색은 MemberCredentialRepository로 한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member", uniqueConstraints = @UniqueConstraint(columnNames = "phone"))
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

    private LocalDateTime claimedAt;

    @Column(nullable = false)
    private boolean platformAdmin = false;

    public boolean isPlatformAdmin() { return platformAdmin; }

    public void grantPlatformAdmin() { this.platformAdmin = true; }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    /** 클럽이 등록하는 익명 회원 */
    public static Member anonymous() {
        return new Member();
    }

    // anonymous 팩토리에 전화번호 받는 버전 추가
    public static Member anonymousWithPhone(String phone) {
        Member m = new Member();
        m.phone = phone;
        return m;
    }

    /**
     * 익명 회원을 계정으로 전환.
     * 인증 수단(MemberCredential)은 이 메서드 호출 후 별도로 저장한다
     * (credential.of***(member, ...) → credentialRepository.save).
     */
    public void claim(String name) {
        if (isClaimed()) {
            throw new IllegalStateException("이미 계정이 연결된 회원입니다.");
        }
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        this.claimedAt = LocalDateTime.now();
    }

    public boolean isClaimed() {
        return claimedAt != null;
    }

    public void updateProfile(String name, Gender gender, Short birthYear) {
        this.name = name;
        this.gender = gender;
        this.birthYear = birthYear;
    }

    public static Member withPhone(String phone) {
        Member m = new Member();
        m.phone = phone;
        return m;
    }

    public void updateName(String name) {
        this.name = (name == null || name.isBlank()) ? null : name.trim();
    }
}