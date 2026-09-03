package com.iamnot.fitmeasure.config.security;

import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** 로그인한 트레이너/사장. 어느 클럽 소속인지 함께 들고 있다. */
public class LoginMember implements UserDetails {

    private final Long memberId;
    private final Long clubId;
    private final String phone;
    private final String passwordHash;
    private final MembershipRole role;
    private final String nickname;

    public LoginMember(Membership membership) {
        this.memberId = membership.getMember().getId();
        this.clubId = membership.getClub().getId();
        this.phone = membership.getMember().getPhone();
        this.passwordHash = membership.getMember().getPasswordHash();
        this.role = membership.getRole();
        this.nickname = membership.getNickname();
    }

    public Long clubId() { return clubId; }
    public Long memberId() { return memberId; }
    public String nickname() { return nickname; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return phone; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}