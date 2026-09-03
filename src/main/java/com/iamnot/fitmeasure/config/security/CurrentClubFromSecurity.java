package com.iamnot.fitmeasure.config.security;

import com.iamnot.fitmeasure.config.CurrentClub;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;

/** 로그인한 사용자의 클럽을 현재 클럽으로. FixedCurrentClub를 대체. */
@Component
@Primary
public class CurrentClubFromSecurity implements CurrentClub {

    @Override
    public Long clubId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginMember lm) {
            return lm.clubId();
        }
        throw new IllegalStateException("로그인이 필요합니다.");
    }
}