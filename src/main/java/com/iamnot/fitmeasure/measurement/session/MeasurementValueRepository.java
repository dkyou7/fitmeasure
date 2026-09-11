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

    /** 순위 계산용 모집단: 같은 클럽·같은 템플릿·같은 항목의 유효 값 전체 (회원별 최신 선별은 서비스에서) */
    @Query("""
    select v from MeasurementValue v
    join v.session s
    where s.membership.club.id = :clubId
      and s.template.id = :templateId
      and v.templateItem.id = :itemId
      and v.skipped = false
      and v.valueNumber is not null
    order by s.membership.id asc, s.measuredAt desc
""")
    List<MeasurementValue> findClubItemValues(@Param("clubId") Long clubId,
                                              @Param("templateId") Long templateId,
                                              @Param("itemId") Long itemId);
}