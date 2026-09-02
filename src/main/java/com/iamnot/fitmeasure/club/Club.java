package com.iamnot.fitmeasure.club;

import com.iamnot.fitmeasure.config.BaseEntity;
import jakarta.persistence.*;

@Entity
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

    protected Club() {
    }

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

    public void updateInfo(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public ClubType getType() { return type; }
    public ClubPlan getPlan() { return plan; }
    public int getFreeMemberLimit() { return freeMemberLimit; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
}