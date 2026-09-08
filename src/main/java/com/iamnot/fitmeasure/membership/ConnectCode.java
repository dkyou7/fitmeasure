package com.iamnot.fitmeasure.membership;

import com.iamnot.fitmeasure.config.BaseEntity;
import com.iamnot.fitmeasure.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 회원이 발급하는 6자리 연결 코드. 트레이너가 이 코드로 자기 클럽에 회원을 등록한다. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "connect_code", indexes = @Index(name = "idx_connect_code", columnList = "code"))
public class ConnectCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    public ConnectCode(Member member, String code, LocalDateTime expiresAt) {
        this.member = member;
        this.code = code;
        this.expiresAt = expiresAt;
    }

    public boolean isValid() {
        return !used && expiresAt.isAfter(LocalDateTime.now());
    }

    public void markUsed() {
        this.used = true;
    }
}