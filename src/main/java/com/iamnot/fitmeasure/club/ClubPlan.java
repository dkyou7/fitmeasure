package com.iamnot.fitmeasure.club;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClubPlan {
    FREE("무료", 1),
    MONTHLY("유료 월간", 3),
    YEARLY("유료 연간", 3);

    private final String label;
    private final int staffLimit;

    public boolean isPaid() { return this != FREE; }
}