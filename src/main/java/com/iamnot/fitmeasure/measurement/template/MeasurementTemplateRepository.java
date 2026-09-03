package com.iamnot.fitmeasure.measurement.template;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MeasurementTemplateRepository extends JpaRepository<MeasurementTemplate, Long> {

    /** 시스템 표준 템플릿 목록 (club_id IS NULL) */
    List<MeasurementTemplate> findByClubIsNull();

    /** 클럽의 템플릿 목록 */
    List<MeasurementTemplate> findByClubId(Long clubId);

    /** 클럽의 기본 템플릿 */
    Optional<MeasurementTemplate> findByClubIdAndIsDefaultTrue(Long clubId);

    /** 격리 조회 */
    Optional<MeasurementTemplate> findByIdAndClubId(Long id, Long clubId);
}