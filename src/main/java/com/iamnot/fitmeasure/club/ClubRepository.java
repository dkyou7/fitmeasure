package com.iamnot.fitmeasure.club;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {
    Optional<Club> findBySlug(String slug);

    List<Club> findByListedTrue();
}