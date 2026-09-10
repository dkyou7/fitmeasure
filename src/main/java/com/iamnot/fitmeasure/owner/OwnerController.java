package com.iamnot.fitmeasure.owner;

import com.iamnot.fitmeasure.club.*;
import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final CurrentClub currentClub;
    private final ClubRepository clubRepository;
    private final MembershipRepository membershipRepository;
    private final StaffService staffService;
    private final ClubService clubService;
    private final OwnerService ownerService;

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

    @GetMapping("/plans")
    public String plans(Model model) {
        Long clubId = currentClub.clubId();
        var club = clubRepository.findById(clubId).orElseThrow();
        long memberCount = membershipRepository.countByClubIdAndRole(clubId, MembershipRole.MEMBER);

        model.addAttribute("currentPlan", club.getPlan().name());
        model.addAttribute("memberCount", memberCount);
        return "owner/plans";
    }

    @GetMapping("/staff")
    public String staff(Model model) {
        model.addAttribute("staff", staffService.listStaff());
        return "owner/staff";
    }

    @PostMapping("/staff")
    public String connectStaff(@RequestParam String code, Model model) {
        staffService.connectStaffByCode(code);
        model.addAttribute("staff", staffService.listStaff());
        return "owner/staff :: staffTable";
    }

    @PostMapping("/staff/{id}/promote")
    public String promoteStaff(@PathVariable Long id, Model model) {
        staffService.promote(id);
        model.addAttribute("staff", staffService.listStaff());
        return "owner/staff :: staffTable";
    }

    @PostMapping("/staff/{id}/demote")
    public String demoteStaff(@PathVariable Long id, Model model) {
        staffService.demote(id);
        model.addAttribute("staff", staffService.listStaff());
        return "owner/staff :: staffTable";
    }

    @GetMapping("/club")
    public String clubForm(Model model) {
        Long clubId = currentClub.clubId();
        Club club = clubRepository.findById(clubId).orElseThrow();
        model.addAttribute("club", club);
        model.addAttribute("types", ClubType.values());
        return "owner/club-edit";
    }

    @PostMapping("/club")
    public String updateClub(@RequestParam String name,
                             @RequestParam(required = false) String address,
                             @RequestParam(required = false) String intro,
                             @RequestParam(defaultValue = "false") boolean listed) {
        clubService.updateClub(currentClub.clubId(), name, address, intro, listed);
        return "redirect:/owner/club?saved";
    }

    @PostMapping("/billing/request")
    public String requestPlan(@RequestParam ClubPlan plan) {
        ownerService.requestPlan(currentClub.clubId(), plan);
        return "redirect:/owner/billing?requested";
    }

    @PostMapping("/billing/cancel")
    public String cancelPlan() {
        ownerService.cancelRequestPlan(currentClub.clubId());
        return "redirect:/owner/billing";
    }
}