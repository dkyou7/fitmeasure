package com.iamnot.fitmeasure.club;

import com.iamnot.fitmeasure.club.dto.ClubDetail;
import com.iamnot.fitmeasure.club.dto.ClubListRow;
import com.iamnot.fitmeasure.club.dto.ProgramInfo;
import com.iamnot.fitmeasure.measurement.template.MeasurementTemplateRepository;
import com.iamnot.fitmeasure.measurement.template.TemplateItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubDiscoveryService {

    private final ClubRepository clubRepository;
    private final MeasurementTemplateRepository templateRepository;

    /** 회원에게 노출되는(공개 동의한) 헬스장 목록 */
    @Transactional(readOnly = true)
    public List<ClubListRow> listedClubs() {
        return clubRepository.findByListedTrue().stream()
                .map(c -> new ClubListRow(c.getId(), c.getName(), c.getAddress(), c.getType().name()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ClubDetail getPublicDetail(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .filter(Club::isListed)   // 공개된 것만
                .orElseThrow(() -> new IllegalArgumentException("헬스장을 찾을 수 없습니다."));

        List<ProgramInfo> programs = templateRepository.findByClubId(clubId).stream()
                .map(t -> new ProgramInfo(
                        t.getName(),
                        t.getItems().stream().filter(TemplateItem::isActive)
                                .map(TemplateItem::getName).toList()))
                .toList();

        return new ClubDetail(club.getName(), club.getAddress(), club.getIntro(),
                club.getType().name(), programs);
    }
}