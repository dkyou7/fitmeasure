package com.iamnot.fitmeasure.api.auth;

import com.iamnot.fitmeasure.config.security.AppPrincipal;

/** JWT의 memberId로 principal을 복원한다. */
public interface AppPrincipalLoader {
    AppPrincipal load(Long memberId);
}