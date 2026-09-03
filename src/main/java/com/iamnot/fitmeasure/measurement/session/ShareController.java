package com.iamnot.fitmeasure.measurement.session;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** 로그인 없이 열리는 공개 공유 카드 */
@Controller
@RequiredArgsConstructor
public class ShareController {

    private final MeasurementService measurementService;

    @GetMapping("/s/{token}")
    public String card(@PathVariable String token, Model model) {
        model.addAttribute("card", measurementService.getShareCard(token));
        return "share/card";
    }
}