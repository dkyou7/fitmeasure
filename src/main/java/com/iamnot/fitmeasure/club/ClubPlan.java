package com.iamnot.fitmeasure.club;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClubPlan {
    FREE("무료"),
    MONTHLY("유료 월간"),
    YEARLY("유료 연간");

    private final String label;
    public boolean isPaid() { return this != FREE; }
}