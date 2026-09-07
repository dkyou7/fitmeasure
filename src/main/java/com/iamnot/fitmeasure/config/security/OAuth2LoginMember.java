package com.iamnot.fitmeasure.config.security;

import com.iamnot.fitmeasure.member.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class OAuth2LoginMember implements OAuth2User, AppPrincipal {
    private final Long memberId;
    private final String nickname;
    private final Map<String, Object> attributes;

    public OAuth2LoginMember(Member member, Map<String, Object> attributes) {
        this.memberId = member.getId();
        this.nickname = member.getName() != null ? member.getName() : "회원";
        this.attributes = attributes;
    }

    public Long memberId() { return memberId; }
    public String nickname() { return nickname; }

    @Override
    public boolean isPlatformAdmin() {
        return false;
    }

    public Long clubId() { return null; }   // 카카오 신규는 클럽 없음(떠도는 계정)

    @Override public Map<String, Object> getAttributes() { return attributes; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    @Override public String getName() { return String.valueOf(memberId); }
}