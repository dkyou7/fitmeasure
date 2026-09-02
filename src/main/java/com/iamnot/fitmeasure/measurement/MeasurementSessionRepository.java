package com.iamnot.fitmeasure.measurement;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MeasurementSessionRepository extends JpaRepository<MeasurementSession, Long> {

    /** 회원의 측정 이력 (최신순) */
    List<MeasurementSession> findByMembershipIdOrderByMeasuredAtDesc(Long membershipId);

    /** 회원의 가장 최근 측정 (다음 측정 예정일 계산용) */
    Optional<MeasurementSession> findFirstByMembershipIdOrderByMeasuredAtDesc(Long membershipId);

    /** 공유 카드 조회 (공개 링크) */
    Optional<MeasurementSession> findByShareTokenAndShareEnabledTrue(String shareToken);
}