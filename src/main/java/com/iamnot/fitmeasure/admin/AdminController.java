package com.iamnot.fitmeasure.admin;

import com.iamnot.fitmeasure.club.ClubApplicationService;
import com.iamnot.fitmeasure.club.ClubType;
import com.iamnot.fitmeasure.config.security.AppPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ClubApplicationService clubApplicationService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.stats());
        model.addAttribute("clubs", adminService.listClubs());
        model.addAttribute("applications", clubApplicationService.listPending());
        return "admin/dashboard";
    }

    @PostMapping("/clubs/{id}/toggle-plan")
    public String togglePlan(@PathVariable Long id, Model model) {
        adminService.togglePlan(id);
        model.addAttribute("clubs", adminService.listClubs());
        return "admin/dashboard :: clubTable";
    }

    @GetMapping("/clubs/new")
    public String newClubForm(Model model) {
        model.addAttribute("types", ClubType.values());
        model.addAttribute("searched", false);   // 추가
        return "admin/club-new";
    }

    /** 아이디 검색 (htmx) */
    @GetMapping("/clubs/new/search")
    public String search(@RequestParam String username, Model model) {
        model.addAttribute("result", adminService.findByUsername(username));
        model.addAttribute("searched", true);
        model.addAttribute("types", ClubType.values());
        return "admin/club-new :: searchResult";
    }

    @PostMapping("/clubs")
    public String createClub(@RequestParam Long memberId,
                             @RequestParam String clubName,
                             @RequestParam ClubType type,
                             Model model) {
        adminService.createClubForExistingMember(memberId, clubName, type);
        return "redirect:/admin";
    }

    @GetMapping("/members")
    public String members(@RequestParam(required = false) String keyword,
                          @PageableDefault(size = 20) Pageable pageable,
                          Model model) {
        model.addAttribute("page", adminService.listMembers(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "admin/members";
    }

    @PostMapping("/members/{id}/grant-admin")
    public String grantAdmin(@PathVariable Long id,
                             @RequestParam(required = false) String keyword,
                             @PageableDefault(size = 20) Pageable pageable,
                             Model model) {
        adminService.grantPlatformAdmin(id);
        model.addAttribute("page", adminService.listMembers(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "admin/members :: memberTable";
    }

    @PostMapping("/members/{id}/revoke-admin")
    public String revokeAdmin(@PathVariable Long id,
                              @AuthenticationPrincipal AppPrincipal principal,
                              @RequestParam(required = false) String keyword,
                              @PageableDefault(size = 20) Pageable pageable,
                              Model model) {
        adminService.revokePlatformAdmin(id, principal.memberId());
        model.addAttribute("page", adminService.listMembers(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "admin/members :: memberTable";
    }

    /**
    * Club 오픈 승인
    * */
    @PostMapping("/applications/{id}/approve")
    public String approveApplication(@PathVariable Long id) {
        clubApplicationService.approve(id);
        return "redirect:/admin";
    }

    @PostMapping("/applications/{id}/reject")
    public String rejectApplication(@PathVariable Long id) {
        clubApplicationService.reject(id);
        return "redirect:/admin";
    }
}