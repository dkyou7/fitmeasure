package com.iamnot.fitmeasure.measurement.session;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

/** 공유 링크용 추측 불가능한 토큰 생성 */
@Component
public class ShareTokenGenerator {

    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}