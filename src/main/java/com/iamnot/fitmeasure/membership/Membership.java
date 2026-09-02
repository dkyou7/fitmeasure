package com.iamnot.fitmeasure.membership;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.config.BaseEntity;
import com.iamnot.fitmeasure.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * "이 사람이 이 클럽에 소속된 관계". 측정 세션은 여기에 귀속된다.
 * 역할은 사람이 아니라 관계의 속성이다 (A클럽 STAFF, B클럽 MEMBER 가능).
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "membership",
       uniqueConstraints = @UniqueConstraint(columnNames = {"club_id", "member_id"}),
       indexes = {
           @Index(name = "idx_membership_club_role", columnList = "club_id, role"),
           @Index(name = "idx_membership_club_status", columnList = "club_id, status")
       })
public class Membership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MembershipRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    /** 클럽 안에서의 표시명. 익명 회원은 자동 생성된 별명 */
    @Column(nullable = false, length = 30)
    private String nickname;

    /** 클럽 자체 회원번호 (선택) */
    @Column(length = 30)
    private String memberNo;

    @Column(nullable = false)
    private LocalDate joinedAt;

    @Column(columnDefinition = "TEXT")
    private String memo;

    public Membership(Club club, Member member, MembershipRole role, String nickname) {
        this.club = club;
        this.member = member;
        this.role = role;
        this.nickname = nickname;
        this.joinedAt = LocalDate.now();
    }

    public boolean canMeasure() {
        return role == MembershipRole.OWNER || role == MembershipRole.STAFF;
    }

    public boolean isOwner() {
        return role == MembershipRole.OWNER;
    }

    public void deactivate() {
        this.status = MembershipStatus.DORMANT;
    }

    public void activate() {
        this.status = MembershipStatus.ACTIVE;
    }
}