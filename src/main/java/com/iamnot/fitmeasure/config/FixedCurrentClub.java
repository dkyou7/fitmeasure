package com.iamnot.fitmeasure.config;

import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.club.ClubStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 임시 구현: slug "demo-gym" 클럽을 현재 클럽으로 고정한다.
 * TODO: 커밋 8+ Security 도입 시 세션 기반 구현으로 교체.
 */
@Component
@RequiredArgsConstructor
public class FixedCurrentClub implements CurrentClub {

    private final ClubRepository clubRepository;

    @Override
    public Long clubId() {
        return clubRepository.findBySlugAndStatus("demo-gym", ClubStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("데모 클럽이 시드되지 않았습니다."))
                .getId();
    }
}