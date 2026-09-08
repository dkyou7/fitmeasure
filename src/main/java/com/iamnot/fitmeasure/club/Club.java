package com.iamnot.fitmeasure.club;

import com.iamnot.fitmeasure.config.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

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

    /** 공유 카드 URL 등에 쓰는 식별자 */
    @Column(nullable = false, length = 50)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClubType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClubPlan plan = ClubPlan.FREE;

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



    public Club(String name, String slug, ClubType type) {
        this.name = name;
        this.slug = slug;
        this.type = type;
    }

    public boolean isFree() {
        return plan == ClubPlan.FREE;
    }
    public void upgradeToPaid() {
        this.plan = ClubPlan.PAID;
    }
    public void downgradeToFree() { this.plan = ClubPlan.FREE; }
    public void setListed(boolean listed) { this.listed = listed; }
    public void updateIntro(String intro) { this.intro = intro; }

    public void updateInfo(String name, String address, String intro, boolean listed) {
        this.name = name;
        this.address = address;
        this.intro = intro;
        this.listed = listed;
    }
}