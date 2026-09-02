package com.iamnot.fitmeasure.measurement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/template")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public String view(Model model) {
        model.addAttribute("template", templateService.getDefaultTemplate());
        model.addAttribute("types", MeasurementType.values());
        model.addAttribute("directions", ScoreDirection.values());
        model.addAttribute("categories", FitnessCategory.values());
        return "template/view";
    }

    @PostMapping("/items")
    public String addItem(@RequestParam String name,
                          @RequestParam MeasurementType measurementType,
                          @RequestParam(required = false) String unit,
                          @RequestParam ScoreDirection direction,
                          @RequestParam FitnessCategory category,
                          Model model) {
        templateService.addItem(name, measurementType, unit, direction, category);
        return refreshFragment(model);
    }

    @PostMapping("/items/{id}/deactivate")
    public String deactivate(@PathVariable Long id, Model model) {
        templateService.deactivateItem(id);
        return refreshFragment(model);
    }

    private String refreshFragment(Model model) {
        model.addAttribute("template", templateService.getDefaultTemplate());
        return "template/view :: itemTable";
    }
}