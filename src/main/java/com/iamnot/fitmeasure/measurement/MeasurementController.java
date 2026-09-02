package com.iamnot.fitmeasure.measurement;

import com.iamnot.fitmeasure.measurement.dto.ProgramRow;
import lombok.RequiredArgsConstructor;
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
                       @RequestParam Map<String, String> allParams) {
        Map<Long, String> values = allParams.entrySet().stream()
                .filter(e -> e.getKey().startsWith("item_"))
                .collect(Collectors.toMap(
                        e -> Long.valueOf(e.getKey().substring(5)),
                        Map.Entry::getValue));
        String note = allParams.get("note");
        Long sessionId = measurementService.save(membershipId, programId, values, note);
        return "redirect:/measure/result/" + sessionId;  // 결과 화면은 커밋 11
    }
}