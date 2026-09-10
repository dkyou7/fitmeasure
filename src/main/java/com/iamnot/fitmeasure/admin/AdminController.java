package com.iamnot.fitmeasure.admin;

import com.iamnot.fitmeasure.club.ClubApplicationService;
import com.iamnot.fitmeasure.club.ClubPlan;
import com.iamnot.fitmeasure.club.ClubType;
import com.iamnot.fitmeasure.config.security.AppPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    @PostMapping("/clubs/{id}/approve-plan")
    public String approvePlan(@PathVariable Long id,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt) {
        adminService.approvePlan(id, expiresAt);
        return "redirect:/admin";
    }

    @PostMapping("/clubs/{id}/expire-plan")
    public String expirePlan(@PathVariable Long id) {
        adminService.expireToFree(id);
        return "redirect:/admin";
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

    @GetMapping("/clubs/{id}")
    public String clubDetail(@PathVariable Long id, Model model) {
        model.addAttribute("club", adminService.clubDetail(id));
        model.addAttribute("plans", ClubPlan.values());
        return "admin/club-detail";
    }

    @PostMapping("/clubs/{id}/plan")
    public String changePlan(@PathVariable Long id,
                             @RequestParam ClubPlan plan,
                             @RequestParam(required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt) {
        adminService.changePlan(id, plan, expiresAt);
        return "redirect:/admin/clubs/" + id + "?saved";
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

    @PostMapping("/clubs/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam(required = false)
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt) {
        // 만료일 미입력 시 신청 플랜 기준 기본값(월간+1개월/연간+1년)은 서비스에서 처리하거나 여기서
        adminService.approvePlan(id, expiresAt);
        return "redirect:/admin";
    }

    @PostMapping("/clubs/{id}/expire")
    public String expire(@PathVariable Long id) {
        adminService.expireToFree(id);
        return "redirect:/admin";
    }
}