package com.iamnot.fitmeasure.club;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findBySlugAndStatus(String slug, ClubStatus status);

    List<Club> findByListedTrueAndStatus(ClubStatus status);

    List<Club> findByStatus(ClubStatus status);

    boolean existsByApplicantMemberIdAndStatus(Long applicantMemberId, ClubStatus status);
}