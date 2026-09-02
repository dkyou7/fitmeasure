package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.config.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 자연인. phone이 null이면 클럽이 만들어둔 익명 회원,
 * phone이 채워지면 본인이 claim한 계정이다.
 */
@Entity
@Table(name = "member", uniqueConstraints = @UniqueConstraint(columnNames = "phone"))
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String passwordHash;

    @Column(length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    private Short birthYear;

    private LocalDateTime claimedAt;

    protected Member() {
    }

    /** 클럽이 등록하는 익명 회원 */
    public static Member anonymous() {
        return new Member();
    }

    /** 익명 회원을 본인이 계정으로 전환 */
    public void claim(String phone, String passwordHash, String name) {
        if (isClaimed()) {
            throw new IllegalStateException("이미 계정이 연결된 회원입니다.");
        }
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.name = name;
        this.claimedAt = LocalDateTime.now();
    }

    public boolean isClaimed() {
        return phone != null;
    }

    public void updateProfile(String name, Gender gender, Short birthYear) {
        this.name = name;
        this.gender = gender;
        this.birthYear = birthYear;
    }

    public Long getId() { return id; }
    public String getPhone() { return phone; }
    public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
    public Gender getGender() { return gender; }
    public Short getBirthYear() { return birthYear; }
    public LocalDateTime getClaimedAt() { return claimedAt; }
}