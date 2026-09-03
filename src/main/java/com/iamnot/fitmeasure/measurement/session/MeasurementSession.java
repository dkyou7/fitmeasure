package com.iamnot.fitmeasure.measurement.session;

import com.iamnot.fitmeasure.config.BaseEntity;
import com.iamnot.fitmeasure.measurement.template.MeasurementTemplate;
import com.iamnot.fitmeasure.membership.Membership;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 1회 측정 이벤트. "이 클럽에서의 이 회원"(membership)에 귀속된다.
 * 측정값들을 소유하며(cascade), 항목 기준으로 읽으면 성장 추이가 된다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "measurement_session",
       indexes = @Index(name = "idx_session_membership_measured",
                        columnList = "membership_id, measured_at"))
public class MeasurementSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 측정 대상 (역할 MEMBER인 membership) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    /** 어느 측정표로 쟀는지 (당시 기준 보존) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private MeasurementTemplate template;

    /** 측정한 사람 (OWNER/STAFF membership) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "measured_by_id", nullable = false)
    private Membership measuredBy;

    /** 측정 시각 (소급 입력 대비 created_at과 분리) */
    @Column(nullable = false)
    private LocalDateTime measuredAt;

    /** 공유 카드 공개 링크 토큰 */
    @Column(length = 32, unique = true)
    private String shareToken;

    /** 회원이 공유를 허용했는지 (기본 비공개) */
    @Column(nullable = false)
    private boolean shareEnabled = false;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeasurementValue> values = new ArrayList<>();

    public MeasurementSession(Membership membership, MeasurementTemplate template,
                              Membership measuredBy, LocalDateTime measuredAt) {
        this.membership = membership;
        this.template = template;
        this.measuredBy = measuredBy;
        this.measuredAt = measuredAt;
    }

    public void addValue(MeasurementValue value) {
        values.add(value);
        value.assignSession(this);
    }

    public void enableShare(String token) {
        this.shareToken = token;
        this.shareEnabled = true;
    }

    public void disableShare() {
        this.shareEnabled = false;
    }

    public void updateNote(String note) {
        this.note = note;
    }
}