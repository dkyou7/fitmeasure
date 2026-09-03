package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.config.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원의 인증 수단 하나. 한 Member가 여러 개 가질 수 있다(휴대폰+카카오 등).
 * PHONE이면 providerId=휴대폰번호 + passwordHash,
 * 소셜이면 providerId=소셜 고유 id, passwordHash=null.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_credential",
       uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"}))
public class MemberCredential extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuthProvider provider;

    /** PHONE=휴대폰번호, 소셜=provider의 고유 사용자 id */
    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    /** PHONE일 때만 */
    @Column(length = 255)
    private String passwordHash;

    /** 소셜에서 받은 이메일(참고용, 없을 수 있음) */
    @Column(length = 100)
    private String email;

    private MemberCredential(Member member, AuthProvider provider, String providerId) {
        this.member = member;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static MemberCredential social(Member member, AuthProvider provider,
                                          String providerId, String email) {
        MemberCredential c = new MemberCredential(member, provider, providerId);
        c.email = email;
        return c;
    }

    public static MemberCredential username(Member member, String username, String passwordHash) {
        MemberCredential c = new MemberCredential(member, AuthProvider.USERNAME, username);
        c.passwordHash = passwordHash;
        return c;
    }

}