package com.iamnot.fitmeasure.measurement;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.config.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 측정표. club이 null이면 시스템 표준 템플릿(읽기전용),
 * club이 있으면 그 클럽이 복사해 커스텀한 템플릿.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "measurement_template")
public class MeasurementTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** null이면 시스템 표준 템플릿 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @Column(nullable = false, length = 100)
    private String name;

    /** 어느 표준 템플릿에서 복사했는지 (표준이면 null) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_template_id")
    private MeasurementTemplate sourceTemplate;

    /** 권장 측정 주기(일). 기본 4주 */
    @Column(nullable = false)
    private int recommendedCadenceDays = 28;

    /** 클럽의 기본 측정표 여부 */
    @Column(nullable = false)
    private boolean isDefault = false;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<TemplateItem> items = new ArrayList<>();

    /** 시스템 표준 템플릿 생성 */
    public static MeasurementTemplate standard(String name) {
        MeasurementTemplate t = new MeasurementTemplate();
        t.name = name;
        return t;
    }

    /** 표준 템플릿을 클럽용으로 복사 (항목까지 깊은 복사) */
    public MeasurementTemplate copyForClub(Club club) {
        MeasurementTemplate copy = new MeasurementTemplate();
        copy.club = club;
        copy.name = this.name;
        copy.sourceTemplate = this;
        copy.recommendedCadenceDays = this.recommendedCadenceDays;
        for (TemplateItem item : this.items) {
            copy.addItem(item.copyForTemplate(copy));
        }
        return copy;
    }

    public boolean isStandard() {
        return club == null;
    }

    public void addItem(TemplateItem item) {
        items.add(item);
        item.assignTemplate(this);
    }

    public void rename(String name) {
        this.name = name;
    }

    public void changeCadence(int days) {
        this.recommendedCadenceDays = days;
    }

    public void markAsDefault() {
        this.isDefault = true;
    }

    /** 클럽이 새로 만드는 빈 프로그램 */
    public static MeasurementTemplate forClub(Club club, String name, int cadenceDays) {
        MeasurementTemplate t = new MeasurementTemplate();
        t.club = club;
        t.name = name;
        t.recommendedCadenceDays = cadenceDays;
        return t;
    }
}