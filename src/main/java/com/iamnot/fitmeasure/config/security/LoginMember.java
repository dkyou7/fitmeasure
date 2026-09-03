package com.iamnot.fitmeasure.config.security;

import com.iamnot.fitmeasure.member.Member;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** 로그인한 트레이너/사장. 소속 클럽을 함께 들고 있다. */
public class LoginMember implements UserDetails {

    private final Long memberId;
    private final Long clubId;
    private final String username;      // 휴대폰번호
    private final String passwordHash;
    private final MembershipRole role;
    private final String nickname;

    public LoginMember(Member member, String username, String passwordHash, Membership membership) {
        this.memberId = member.getId();
        this.username = username;
        this.passwordHash = passwordHash;
        if (membership != null) {
            this.clubId = membership.getClub().getId();
            this.role = membership.getRole();
            this.nickname = membership.getNickname();
        } else {
            this.clubId = null;          // 떠도는 계정
            this.role = null;
            this.nickname = member.getName() != null ? member.getName() : "회원";
        }
    }

    public Long clubId() { return clubId; }
    public Long memberId() { return memberId; }
    public String nickname() { return nickname; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));  // 떠도는 계정 기본 권한
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}