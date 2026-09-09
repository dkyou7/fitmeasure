package com.iamnot.fitmeasure.club;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClubApplicationService {

    private final ClubRepository clubRepository;

    /** 사용자가 헬스장 오픈 신청 (PENDING 생성) */
    @Transactional
    public void apply(Long memberId, String name, ClubType type, String phone, String address) {
        if (clubRepository.existsByApplicantMemberIdAndStatus(memberId, ClubStatus.PENDING)) {
            throw new IllegalStateException("이미 처리 대기 중인 신청이 있어요.");
        }
        clubRepository.save(Club.apply(name.trim(), type, phone, address, memberId));
    }
}