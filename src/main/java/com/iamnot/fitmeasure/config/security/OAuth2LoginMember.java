package com.iamnot.fitmeasure.config.security;

import com.iamnot.fitmeasure.member.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class OAuth2LoginMember implements OAuth2User, AppPrincipal {
    private final Long memberId;
    private final String nickname;
    private final boolean onboarded;
    private final boolean platformAdmin;
    private final Map<String, Object> attributes;

    public OAuth2LoginMember(Member member, Map<String, Object> attributes) {
        this.memberId = member.getId();
        this.nickname = member.getName() != null ? member.getName() : "회원";
        this.onboarded = member.isOnboarded();
        this.platformAdmin = member.isPlatformAdmin();
        this.attributes = attributes;
    }

    @Override public Long memberId() { return memberId; }
    @Override public String nickname() { return nickname; }
    public String getNickname() { return nickname; }         // sec:authentication="principal.nickname" 용
    @Override public Long clubId() { return null; }          // 카카오 신규는 클럽 없음
    @Override public boolean isPlatformAdmin() { return platformAdmin; }
    @Override public boolean isOnboarded() { return onboarded; }

    @Override public Map<String, Object> getAttributes() { return attributes; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> auths = new ArrayList<>();
        auths.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (platformAdmin) {
            auths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return auths;
    }
    @Override public String getName() { return String.valueOf(memberId); }
}