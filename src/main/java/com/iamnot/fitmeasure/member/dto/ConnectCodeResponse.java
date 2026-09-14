package com.iamnot.fitmeasure.member.dto;

import java.time.LocalDateTime;

/** 연결 코드와 만료 시각. 앱은 expiresAt으로 카운트다운한다. */
public record ConnectCodeResponse(String code, LocalDateTime expiresAt) {
}