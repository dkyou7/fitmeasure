package com.iamnot.fitmeasure.club;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;

    @Transactional
    public void updateClub(Long clubId, String name, String address, String intro, boolean listed) {
        Club club = clubRepository.findById(clubId).orElseThrow();
        club.updateInfo(name, address, intro, listed);
    }
}