package com.iamnot.fitmeasure.owner;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubPlan;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.membership.*;
import com.iamnot.fitmeasure.owner.dto.StaffRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final ClubRepository clubRepository;

    @Transactional
    public void requestPlan(Long clubId, ClubPlan target) {
        Club club = clubRepository.findById(clubId).orElseThrow();
        club.requestPlan(target);
    }

    @Transactional
    public void cancelRequestPlan(Long clubId) {
        Club club = clubRepository.findById(clubId).orElseThrow();
        club.cancelRequest();
    }
}