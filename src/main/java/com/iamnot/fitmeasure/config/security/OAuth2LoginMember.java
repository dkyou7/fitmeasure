package com.iamnot.fitmeasure.config.security;

import com.iamnot.fitmeasure.member.Member;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class OAuth2LoginMember implements OAuth2User, AppPrincipal {
    private final Long memberId;
    private final Long clubId;
    private final MembershipRole role;
    private final String nickname;
    private final boolean onboarded;
    private final boolean platformAdmin;
    private final Map<String, Object> attributes;

    public OAuth2LoginMember(Member member, Membership membership, Map<String, Object> attributes) {
        this.memberId = member.getId();
        this.onboarded = member.isOnboarded();
        this.platformAdmin = member.isPlatformAdmin();
        this.attributes = attributes;
        if (membership != null) {
            this.clubId = membership.getClub().getId();
            this.role = membership.getRole();
            this.nickname = membership.getNickname();
        } else {
            this.clubId = null;
            this.role = null;
            this.nickname = member.getName() != null ? member.getName() : "회원";
        }
    }

    @Override public Long memberId() { return memberId; }
    @Override public String nickname() { return nickname; }
    public String getNickname() { return nickname; }
    @Override public Long clubId() { return clubId; }
    @Override public boolean isPlatformAdmin() { return platformAdmin; }
    @Override public boolean isOnboarded() { return onboarded; }

    @Override public Map<String, Object> getAttributes() { return attributes; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> auths = new ArrayList<>();
        if (platformAdmin) {
            auths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        if (role != null) {
            auths.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        } else {
            auths.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return auths;
    }
    @Override public String getName() { return String.valueOf(memberId); }
}