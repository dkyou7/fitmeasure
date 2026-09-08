package com.iamnot.fitmeasure.membership;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConnectCodeRepository extends JpaRepository<ConnectCode, Long> {
    Optional<ConnectCode> findFirstByCodeAndUsedFalseOrderByCreatedAtDesc(String code);
}