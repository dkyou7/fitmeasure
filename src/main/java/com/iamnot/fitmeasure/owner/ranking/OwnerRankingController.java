package com.iamnot.fitmeasure.owner.ranking;

import com.iamnot.fitmeasure.config.CurrentClub;
import com.iamnot.fitmeasure.measurement.template.TemplateItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerRankingController {

    private final CurrentClub currentClub;
    private final OwnerRankingService ownerRankingService;

    @GetMapping("/ranking")
    public String ranking(@RequestParam(required = false) Long itemId, Model model) {
        Long clubId = currentClub.clubId();
        List<TemplateItem> items = ownerRankingService.rankableItems(clubId);

        model.addAttribute("items", items);
        if (items.isEmpty()) {
            return "owner/ranking";   // 측정 기록 없음 상태
        }

        // 타 클럽 itemId 주입 방어 — 목록에 없으면 첫 항목으로
        TemplateItem selected = items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElse(items.get(0));

        model.addAttribute("selected", selected);
        model.addAttribute("rows", ownerRankingService.absoluteRanking(clubId, selected));
        return "owner/ranking";
    }
}