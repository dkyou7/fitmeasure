package com.iamnot.fitmeasure.measurement.template;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/programs")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    /** 프로그램 목록 */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("programs", templateService.listPrograms());
        return "program/list";
    }

    /** 프로그램 생성 → 상세로 이동 */
    @PostMapping
    public String create(@RequestParam String name,
                         @RequestParam(defaultValue = "28") int cadenceDays) {
        Long id = templateService.createProgram(name, cadenceDays);
        return "redirect:/programs/" + id;
    }

    //** 프로그램 상세 (현황 + 회원 순위) — 조회 전용 */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("program", templateService.getProgram(id));
        model.addAttribute("ranking", templateService.getRanking(id));
        return "program/detail";
    }

    /** 프로그램 편집 (항목 관리) */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        addProgramModel(model, id);
        return "program/edit";
    }

    @PostMapping("/{id}/edit/items")
    public String addItem(@PathVariable Long id,
                          @RequestParam String name,
                          @RequestParam MeasurementType measurementType,
                          @RequestParam(required = false) String unit,
                          @RequestParam ScoreDirection direction,
                          @RequestParam FitnessCategory category,
                          Model model) {
        templateService.addItem(id, name, measurementType, unit, direction, category);
        addProgramModel(model, id);
        return "program/edit :: itemTable";
    }

    @PostMapping("/{id}/edit/items/{itemId}")
    public String updateItem(@PathVariable Long id, @PathVariable Long itemId,
                             @RequestParam String name,
                             @RequestParam MeasurementType measurementType,
                             @RequestParam(required = false) String unit,
                             @RequestParam ScoreDirection direction,
                             @RequestParam FitnessCategory category,
                             Model model) {
        templateService.updateItem(id, itemId, name, measurementType, unit, direction, category);
        addProgramModel(model, id);
        return "program/edit :: itemTable";
    }

    @PostMapping("/{id}/edit/items/{itemId}/deactivate")
    public String deactivate(@PathVariable Long id, @PathVariable Long itemId, Model model) {
        templateService.deactivateItem(id, itemId);
        addProgramModel(model, id);
        return "program/edit :: itemTable";
    }

    private void addProgramModel(Model model, Long id) {
        model.addAttribute("program", templateService.getProgram(id));
        model.addAttribute("types", MeasurementType.values());
        model.addAttribute("directions", ScoreDirection.values());
        model.addAttribute("categories", FitnessCategory.values());
    }
}