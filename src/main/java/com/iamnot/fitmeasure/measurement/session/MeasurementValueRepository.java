package com.iamnot.fitmeasure.measurement.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeasurementValueRepository extends JpaRepository<MeasurementValue, Long> {

    /** 특정 회원의 특정 항목 값들을 측정일 순으로 → 성장 추이 */
    @Query("""
        select v from MeasurementValue v
        join v.session s
        where s.membership.id = :membershipId
          and v.templateItem.id = :itemId
          and v.skipped = false
        order by s.measuredAt asc
    """)
    List<MeasurementValue> findTrend(@Param("membershipId") Long membershipId,
                                     @Param("itemId") Long itemId);

    boolean existsByTemplateItemId(Long templateItemId);
}