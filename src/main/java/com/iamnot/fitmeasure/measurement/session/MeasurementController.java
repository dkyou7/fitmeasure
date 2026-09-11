package com.iamnot.fitmeasure.measurement.session;

import com.iamnot.fitmeasure.config.security.AppPrincipal;
import com.iamnot.fitmeasure.config.security.LoginMember;
import com.iamnot.fitmeasure.measurement.session.dto.TrendView;
import com.iamnot.fitmeasure.measurement.template.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/measure")
@RequiredArgsConstructor
public class MeasurementController {

    private final MeasurementService measurementService;
    private final TemplateService templateService;

    /** 1단계: 회원 선택 후 프로그램 고르는 화면 */
    @GetMapping("/{membershipId}")
    public String selectProgram(@PathVariable Long membershipId, Model model) {
        model.addAttribute("membershipId", membershipId);
        model.addAttribute("programs", templateService.listPrograms());
        return "measure/select";
    }

    /** 2단계: 측정 입력 화면 */
    @GetMapping("/{membershipId}/program/{programId}")
    public String form(@PathVariable Long membershipId, @PathVariable Long programId, Model model) {
        model.addAttribute("form", measurementService.prepareForm(membershipId, programId));
        return "measure/form";
    }

    /** 저장: item_{id} 파라미터들을 Map으로 수집 */
    @PostMapping("/{membershipId}/program/{programId}")
    public String save(@PathVariable Long membershipId, @PathVariable Long programId,
                       @AuthenticationPrincipal AppPrincipal loginMember,
                       @RequestParam Map<String, String> allParams) {
        Map<Long, String> values = allParams.entrySet().stream()
                .filter(e -> e.getKey().startsWith("item_"))
                .collect(Collectors.toMap(
                        e -> Long.valueOf(e.getKey().substring(5)),
                        Map.Entry::getValue));
        String note = allParams.get("note");
        Double bodyWeight = parseNullableDouble(allParams.get("bodyWeight"));
        Double height = parseNullableDouble(allParams.get("height"));

        Long sessionId = measurementService.save(
                membershipId, programId, values, note, bodyWeight, height, loginMember);
        return "redirect:/measure/result/" + sessionId;
    }

    /** 빈 값·공백·숫자 아님은 null로 (선택 입력) */
    private Double parseNullableDouble(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Double.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 측정 결과지 */
    @GetMapping("/result/{sessionId}")
    public String result(@PathVariable Long sessionId,
                         @AuthenticationPrincipal AppPrincipal loginMember,
                         Model model) {
        model.addAttribute("result", measurementService.getResult(sessionId, loginMember));
        return "measure/result";
    }

    /** 성장 추이 데이터 (JSON, Chart.js용) */
    @GetMapping("/trend/{membershipId}/item/{itemId}")
    @ResponseBody
    public TrendView trend(
            @PathVariable Long membershipId,
            @AuthenticationPrincipal AppPrincipal loginMember,
            @PathVariable Long itemId) {
        return measurementService.getTrend(membershipId, itemId,loginMember);
    }

    @PostMapping("/result/{sessionId}/share")
    @ResponseBody
    public Map<String, String> share(@PathVariable Long sessionId,
                                     @AuthenticationPrincipal AppPrincipal loginMember) {
        String token = measurementService.enableShare(sessionId, loginMember);
        return Map.of("url", "/s/" + token);
    }
}