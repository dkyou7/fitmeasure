package com.iamnot.fitmeasure.owner;

import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final CurrentClub currentClub;
    private final ClubRepository clubRepository;
    private final MembershipRepository membershipRepository;

    @GetMapping("/billing")
    public String billing(Model model) {
        Long clubId = currentClub.clubId();
        var club = clubRepository.findById(clubId).orElseThrow();

        long memberCount = membershipRepository.countByClubIdAndRole(clubId, MembershipRole.MEMBER);
        long staffCount = membershipRepository.countByClubIdAndRole(clubId, MembershipRole.STAFF);
        int freeLimit = club.getFreeMemberLimit();
        boolean overLimit = memberCount > freeLimit;

        model.addAttribute("clubName", club.getName());
        model.addAttribute("plan", club.getPlan().name());   // FREE / PAID
        model.addAttribute("memberCount", memberCount);
        model.addAttribute("staffCount", staffCount);
        model.addAttribute("freeLimit", freeLimit);
        model.addAttribute("overLimit", overLimit);
        // 목업: 결제 예정일은 임시 계산(다음 달 1일)
        model.addAttribute("nextBillingDate", LocalDate.now().withDayOfMonth(1).plusMonths(1));
        return "owner/billing";
    }
}