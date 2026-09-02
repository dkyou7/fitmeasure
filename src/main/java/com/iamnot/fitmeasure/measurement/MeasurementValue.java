package com.iamnot.fitmeasure.measurement;

import com.iamnot.fitmeasure.config.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 측정값. Session ↔ TemplateItem M:N을 푸는 중간 엔티티.
 * 세션(언제)과 항목 정의(무엇)를 잇는 다리이며, 실측값을 담는다.
 * NUMBER/REPS/TIME은 valueNumber(초 단위), SELECT는 valueText.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "measurement_value",
       uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "template_item_id"}),
       indexes = @Index(name = "idx_value_item_session",
                        columnList = "template_item_id, session_id"))
public class MeasurementValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private MeasurementSession session;

    /** 값이 참조하는 항목 정의 (삭제 불가 — 비활성만) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_item_id", nullable = false)
    private TemplateItem templateItem;

    /** NUMBER/REPS/TIME(초) */
    @Column(precision = 10, scale = 2)
    private BigDecimal valueNumber;

    /** SELECT형 */
    @Column(length = 100)
    private String valueText;

    /** 이번엔 측정 안 한 항목 */
    @Column(nullable = false)
    private boolean skipped = false;

    private MeasurementValue(TemplateItem item) {
        this.templateItem = item;
    }

    public static MeasurementValue ofNumber(TemplateItem item, BigDecimal value) {
        MeasurementValue v = new MeasurementValue(item);
        v.valueNumber = value;
        return v;
    }

    public static MeasurementValue ofText(TemplateItem item, String value) {
        MeasurementValue v = new MeasurementValue(item);
        v.valueText = value;
        return v;
    }

    public static MeasurementValue skipped(TemplateItem item) {
        MeasurementValue v = new MeasurementValue(item);
        v.skipped = true;
        return v;
    }

    void assignSession(MeasurementSession session) {
        this.session = session;
    }
}