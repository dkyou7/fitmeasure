package com.iamnot.fitmeasure.config.security;

/** 폼로그인·소셜로그인 공통 principal. 컨트롤러는 이걸로 받는다. */
public interface AppPrincipal {
    Long memberId();
    Long clubId();
    String nickname();
    boolean isPlatformAdmin();
    boolean isOnboarded();
}