package com.iamnot.fitmeasure.admin;

import com.iamnot.fitmeasure.club.ClubType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.stats());
        model.addAttribute("clubs", adminService.listClubs());
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
        return "admin/club-new";
    }

    @PostMapping("/clubs")
    public String createClub(@RequestParam String clubName,
                             @RequestParam ClubType type,
                             @RequestParam String ownerName,
                             @RequestParam String ownerUsername,
                             @RequestParam String ownerPassword,
                             Model model) {
        try {
            adminService.createClubWithOwner(clubName, type, ownerName, ownerUsername, ownerPassword);
        } catch (IllegalStateException e) {
            model.addAttribute("types", ClubType.values());
            model.addAttribute("error", e.getMessage());
            return "admin/club-new";
        }
        return "redirect:/admin";
    }

    /** 아이디 검색 (htmx) */
    @GetMapping("/clubs/new/search")
    public String search(@RequestParam String username, Model model) {
        model.addAttribute("result", adminService.findByUsername(username));
        model.addAttribute("searched", true);
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
}