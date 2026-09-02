package com.iamnot.fitmeasure.measurement;

import com.iamnot.fitmeasure.config.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 측정 항목 정의. "무엇을 어떻게 재는가"를 담는다.
 * 실제 측정값(MeasurementValue)이 이 정의를 참조한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "template_item")
public class TemplateItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private MeasurementTemplate template;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MeasurementType measurementType;

    @Column(length = 20)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ScoreDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private FitnessCategory category;

    @Column(nullable = false)
    private int sortOrder;

    /** 측정 가이드 (검수자 편차 방지) */
    @Column(columnDefinition = "TEXT")
    private String protocol;

    /** SELECT형 선택지, '|' 구분 */
    @Column(length = 255)
    private String selectOptions;

    /** 비정상값 경고 범위 */
    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal minValue;

    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal maxValue;

    /** 삭제 대신 비활성 (과거 측정값 보존) */
    @Column(nullable = false)
    private boolean active = true;

    @Builder
    private TemplateItem(String name, MeasurementType measurementType, String unit,
                         ScoreDirection direction, FitnessCategory category,
                         int sortOrder, String protocol, String selectOptions,
                         java.math.BigDecimal minValue, java.math.BigDecimal maxValue) {
        this.name = name;
        this.measurementType = measurementType;
        this.unit = unit;
        this.direction = direction;
        this.category = category;
        this.sortOrder = sortOrder;
        this.protocol = protocol;
        this.selectOptions = selectOptions;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /** 복사 (표준 → 클럽 템플릿) */
    public TemplateItem copyForTemplate(MeasurementTemplate target) {
        TemplateItem copy = TemplateItem.builder()
                .name(this.name)
                .measurementType(this.measurementType)
                .unit(this.unit)
                .direction(this.direction)
                .category(this.category)
                .sortOrder(this.sortOrder)
                .protocol(this.protocol)
                .selectOptions(this.selectOptions)
                .minValue(this.minValue)
                .maxValue(this.maxValue)
                .build();
        copy.template = target;
        return copy;
    }

    void assignTemplate(MeasurementTemplate template) {
        this.template = template;
    }

    public void deactivate() {
        this.active = false;
    }

    public void updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}