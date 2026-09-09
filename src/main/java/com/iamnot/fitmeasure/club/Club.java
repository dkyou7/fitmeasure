package com.iamnot.fitmeasure.club;

import com.iamnot.fitmeasure.config.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "club", uniqueConstraints = @UniqueConstraint(columnNames = "slug"))
public class Club extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** 공유 카드 URL 등에 쓰는 식별자. 승인 시점에 생성 (PENDING일 땐 null) */
    @Column(length = 50)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClubType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClubPlan plan = ClubPlan.FREE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClubStatus status = ClubStatus.PENDING;

    /** 신청자(승인 시 이 사람을 OWNER로 연결) */
    private Long applicantMemberId;

    @Column(nullable = false)
    private int freeMemberLimit = 10;

    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String address;

    @Column(nullable = false)
    private boolean listed = false;   // 플랫폼 헬스장 찾기에 노출 여부

    @Column(columnDefinition = "TEXT")
    private String intro;   // 헬스장 소개 (회원에게 노출)

    /** 어드민 직접 생성 (즉시 승인 상태) */
    public Club(String name, String slug, ClubType type) {
        this.name = name;
        this.slug = slug;
        this.type = type;
        this.status = ClubStatus.APPROVED;
    }

    /** 사용자 신청 (PENDING 상태로 생성, slug 없음) */
    public static Club apply(String name, ClubType type, String phone,
                             String address, Long applicantMemberId) {
        Club c = new Club();
        c.name = name;
        c.type = type;
        c.phone = phone;
        c.address = address;
        c.applicantMemberId = applicantMemberId;
        c.status = ClubStatus.PENDING;
        return c;
    }

    public void approve(String slug) {
        if (status != ClubStatus.PENDING) {
            throw new IllegalStateException("대기 중인 신청만 승인할 수 있습니다.");
        }
        this.slug = slug;
        this.status = ClubStatus.APPROVED;
    }

    public void reject() {
        if (status != ClubStatus.PENDING) {
            throw new IllegalStateException("대기 중인 신청만 거절할 수 있습니다.");
        }
        this.status = ClubStatus.REJECTED;
    }

    public boolean isApproved() { return status == ClubStatus.APPROVED; }

    public boolean isFree() {
        return plan == ClubPlan.FREE;
    }
    public void upgradeToPaid() {
        this.plan = ClubPlan.PAID;
    }
    public void downgradeToFree() { this.plan = ClubPlan.FREE; }
    public void updateListed(boolean listed) { this.listed = listed; }
    public void updateIntro(String intro) { this.intro = intro; }

    public void updateInfo(String name, String address, String intro, boolean listed) {
        this.name = name;
        this.address = address;
        this.intro = intro;
        this.listed = listed;
    }
}