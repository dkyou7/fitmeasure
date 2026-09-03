package com.iamnot.fitmeasure;

import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import com.iamnot.fitmeasure.measurement.template.MeasurementTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CurrentClub currentClub;
    private final ClubRepository clubRepository;
    private final MembershipRepository membershipRepository;
    private final MeasurementTemplateRepository templateRepository;

    @GetMapping("/")
    public String home(Model model) {
        Long clubId = currentClub.clubId();
        String clubName = clubRepository.findById(clubId)
                .map(c -> c.getName()).orElse("");

        long memberCount = membershipRepository.countByClubIdAndRole(clubId, MembershipRole.MEMBER);
        long programCount = templateRepository.findByClubId(clubId).size();
        int freeLimit = clubRepository.findById(clubId)
                .map(c -> c.getFreeMemberLimit()).orElse(10);

        model.addAttribute("clubName", clubName);
        model.addAttribute("memberCount", memberCount);
        model.addAttribute("programCount", programCount);
        model.addAttribute("freeLimit", freeLimit);
        model.addAttribute("overLimit", memberCount > freeLimit);
        return "index";
    }
}