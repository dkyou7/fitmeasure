package com.iamnot.fitmeasure.admin;

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
}