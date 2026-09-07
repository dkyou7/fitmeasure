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

    /** claim 카드: 트레이너가 미가입 회원에게 보내는 흡수 유도 카드 */
    @GetMapping("/s/{token}")
    public String card(@PathVariable String token, Model model) {
        model.addAttribute("card", measurementService.getShareCard(token));
        return "share/card";
    }

    /** 자랑 카드: 이미 claim한 회원 본인이 친구에게 뿌리는 카드 */
    @GetMapping("/s/{token}/flex")
    public String flex(@PathVariable String token, Model model) {
        model.addAttribute("card", measurementService.getShareCard(token));
        return "share/flex";
    }
}