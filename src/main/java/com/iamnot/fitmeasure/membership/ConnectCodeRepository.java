package com.iamnot.fitmeasure.membership;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConnectCodeRepository extends JpaRepository<ConnectCode, Long> {
    Optional<ConnectCode> findFirstByCodeAndUsedFalseOrderByCreatedAtDesc(String code);

    @Modifying
    @Query("update ConnectCode c " +
            "set c.used = true " +
            "where c.member.id = :memberId " +
            "and c.used = false")
    void markAllUsedByMemberId(@Param("memberId") Long memberId);
}