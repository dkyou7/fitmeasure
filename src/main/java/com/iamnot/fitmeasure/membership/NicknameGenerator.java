package com.iamnot.fitmeasure.membership;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** 익명 회원용 별명 자동 생성 ("이름 없는 곰" 류) */
@Component
public class NicknameGenerator {

    private static final List<String> ADJECTIVES = List.of(
            "이름 없는", "용감한", "성실한", "묵묵한", "단단한",
            "날쌘", "끈질긴", "차분한", "우직한", "가벼운");

    private static final List<String> ANIMALS = List.of(
            "곰", "호랑이", "매", "사슴", "여우",
            "표범", "독수리", "치타", "황소", "늑대");

    public String generate() {
        String adj = ADJECTIVES.get(ThreadLocalRandom.current().nextInt(ADJECTIVES.size()));
        String animal = ANIMALS.get(ThreadLocalRandom.current().nextInt(ANIMALS.size()));
        return adj + " " + animal;
    }
}