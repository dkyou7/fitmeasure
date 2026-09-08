package com.iamnot.fitmeasure.club;

import com.iamnot.fitmeasure.club.dto.ClubListRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubDiscoveryService {

    private final ClubRepository clubRepository;

    /** 회원에게 노출되는(공개 동의한) 헬스장 목록 */
    @Transactional(readOnly = true)
    public List<ClubListRow> listedClubs() {
        return clubRepository.findByListedTrue().stream()
                .map(c -> new ClubListRow(c.getId(), c.getName(), c.getAddress(), c.getType().name()))
                .toList();
    }
}