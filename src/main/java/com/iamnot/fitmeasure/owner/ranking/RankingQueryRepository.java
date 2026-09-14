package com.iamnot.fitmeasure.owner.ranking;

import com.iamnot.fitmeasure.measurement.session.MeasurementValue;
import com.iamnot.fitmeasure.measurement.template.MeasurementType;
import com.iamnot.fitmeasure.measurement.template.TemplateItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 순위 화면 전용 조회. 기존 리포지토리를 건드리지 않도록 분리했다.
 */
public interface RankingQueryRepository extends Repository<MeasurementValue, Long> {

    /** 이 클럽에 실제 숫자 측정값이 존재하는 항목만 (SELECT형·비활성 제외) */
    @Query("""
            select ti
            from TemplateItem ti
            join ti.template tpl
            where ti.active = true
              and ti.measurementType <> :selectType
              and exists (
                    select 1
                    from MeasurementValue v
                    join v.session s
                    where v.templateItem = ti
                      and s.membership.club.id = :clubId
                      and v.skipped = false
                      and v.valueNumber is not null
              )
            order by tpl.id, ti.sortOrder
            """)
    List<TemplateItem> findRankableItems(@Param("clubId") Long clubId,
                                         @Param("selectType") MeasurementType selectType);

    /** [membershipId, 표시이름, valueNumber, measuredAt] 원시 행. 최고값 선별은 서비스에서 */
    @Query("""
            select s.membership.id, s.membership.member.name, v.valueNumber, s.measuredAt
            from MeasurementValue v
            join v.session s
            where s.membership.club.id = :clubId
              and v.templateItem.id = :itemId
              and v.skipped = false
              and v.valueNumber is not null
            """)
    List<Object[]> findNumericValues(@Param("clubId") Long clubId,
                                     @Param("itemId") Long itemId);
}