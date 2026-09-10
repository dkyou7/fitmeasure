package com.iamnot.fitmeasure.member;

import com.iamnot.fitmeasure.club.Club;
import com.iamnot.fitmeasure.club.ClubRepository;
import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberDetailService memberDetailService;
    private final CurrentClub currentClub;
    private final ClubRepository clubRepository;
    private final MembershipRepository membershipRepository;

    @GetMapping
    public String list(Model model) {
        Long clubId = currentClub.clubId();
        Club club = clubRepository.findById(clubId).orElseThrow();
        long memberCount = membershipRepository.countByClubIdAndRole(clubId, MembershipRole.MEMBER);

        model.addAttribute("members", memberService.listMembers());
        model.addAttribute("overLimit", club.isFree() && memberCount > club.getFreeMemberLimit());
        return "member/list";
    }

    @GetMapping("/{membershipId}")
    public String detail(@PathVariable Long membershipId, Model model) {
        model.addAttribute("detail", memberDetailService.getDetail(membershipId));
        return "member/detail";
    }
}