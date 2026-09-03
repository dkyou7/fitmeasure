package com.iamnot.fitmeasure.member;

public class FreeLimitExceededException extends RuntimeException {
    private final int limit;

    public FreeLimitExceededException(int limit) {
        super("무료 플랜은 회원 " + limit + "명까지 등록할 수 있어요.");
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }
}