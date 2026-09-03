package com.iamnot.fitmeasure.measurement;

import com.iamnot.fitmeasure.measurement.template.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 시스템 표준 측정표(club=null)를 최초 1회 시드한다.
 * 이미 존재하면 아무것도 하지 않는다(멱등).
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class StandardTemplateInitializer implements ApplicationRunner {

    private final MeasurementTemplateRepository templateRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!templateRepository.findByClubIsNull().isEmpty()) {
            log.info("표준 프로그램이 이미 존재하여 시드를 건너뜁니다.");
            return;
        }

        templateRepository.save(bigThree());
        templateRepository.save(basicFitness());
        log.info("표준 프로그램 2개 시드 완료");
    }

    /** 3대 측정 */
    private MeasurementTemplate bigThree() {
        MeasurementTemplate t = MeasurementTemplate.standard("3대 측정");
        t.changeCadence(28);
        int o = 0;
        t.addItem(TemplateItem.builder()
                .name("벤치프레스 1RM").measurementType(MeasurementType.NUMBER).unit("kg")
                .direction(ScoreDirection.HIGHER_BETTER).category(FitnessCategory.STRENGTH)
                .sortOrder(o++)
                .protocol("충분한 워밍업 후 1회 최대 중량. 보조자 필수, 풀 가동범위.")
                .minValue(new BigDecimal("0")).maxValue(new BigDecimal("400"))
                .build());
        t.addItem(TemplateItem.builder()
                .name("스쿼트 1RM").measurementType(MeasurementType.NUMBER).unit("kg")
                .direction(ScoreDirection.HIGHER_BETTER).category(FitnessCategory.STRENGTH)
                .sortOrder(o++)
                .protocol("대퇴가 지면과 평행(패러렐)까지 내려간 1회 최대 중량.")
                .minValue(new BigDecimal("0")).maxValue(new BigDecimal("500"))
                .build());
        t.addItem(TemplateItem.builder()
                .name("데드리프트 1RM").measurementType(MeasurementType.NUMBER).unit("kg")
                .direction(ScoreDirection.HIGHER_BETTER).category(FitnessCategory.STRENGTH)
                .sortOrder(o++)
                .protocol("컨벤셔널. 락아웃 완료 기준 1회 최대 중량.")
                .minValue(new BigDecimal("0")).maxValue(new BigDecimal("500"))
                .build());
        return t;
    }

    /** 기초 체력 테스트 */
    private MeasurementTemplate basicFitness() {
        MeasurementTemplate t = MeasurementTemplate.standard("기초 체력 테스트");
        t.changeCadence(28);
        int o = 0;
        t.addItem(TemplateItem.builder()
                .name("푸시업(1분)").measurementType(MeasurementType.REPS).unit("회")
                .direction(ScoreDirection.HIGHER_BETTER).category(FitnessCategory.ENDURANCE)
                .sortOrder(o++)
                .protocol("1분간 정자세 반복 횟수. 가슴이 주먹 높이까지 내려와야 1회 인정.")
                .minValue(new BigDecimal("0")).maxValue(new BigDecimal("200"))
                .build());
        t.addItem(TemplateItem.builder()
                .name("플랭크").measurementType(MeasurementType.TIME).unit("초")
                .direction(ScoreDirection.HIGHER_BETTER).category(FitnessCategory.CORE)
                .sortOrder(o++)
                .protocol("팔꿈치-발끝 지지, 몸 일직선 유지 시간(초). 엉덩이 무너지면 종료.")
                .minValue(new BigDecimal("0")).maxValue(new BigDecimal("1800"))
                .build());
        t.addItem(TemplateItem.builder()
                .name("1km 러닝").measurementType(MeasurementType.TIME).unit("초")
                .direction(ScoreDirection.LOWER_BETTER).category(FitnessCategory.CARDIO)
                .sortOrder(o++)
                .protocol("트레드밀 또는 트랙 1km 완주 시간(초). 낮을수록 우수.")
                .minValue(new BigDecimal("120")).maxValue(new BigDecimal("1200"))
                .build());
        t.addItem(TemplateItem.builder()
                .name("앉아윗몸앞으로굽히기").measurementType(MeasurementType.NUMBER).unit("cm")
                .direction(ScoreDirection.HIGHER_BETTER).category(FitnessCategory.FLEXIBILITY)
                .sortOrder(o++)
                .protocol("좌전굴. 발끝 기준선에서 손끝이 넘어간 거리(cm). 반동 없이.")
                .minValue(new BigDecimal("-30")).maxValue(new BigDecimal("40"))
                .build());
        return t;
    }
}