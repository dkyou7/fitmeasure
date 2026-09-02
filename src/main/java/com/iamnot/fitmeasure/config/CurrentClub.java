package com.iamnot.fitmeasure.config;

/**
 * 현재 요청의 클럽 컨텍스트.
 * 지금은 시드 클럽으로 고정, 이후 Security 붙으면 세션에서 읽도록 구현만 교체.
 * 모든 격리 조회는 이 값을 clubId로 사용한다.
 */
public interface CurrentClub {
    Long clubId();
}