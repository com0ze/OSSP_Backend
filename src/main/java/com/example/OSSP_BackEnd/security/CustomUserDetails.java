package com.example.OSSP_BackEnd.security;

import com.example.OSSP_BackEnd.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final Long id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    // User 엔티티로부터 CustomUserDetails를 생성하는 생성자
    public CustomUserDetails(User user) {
        this.user = user;
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.authorities = user.getAuthorities();
    }

    // JWT 클레임으로부터 CustomUserDetails를 생성하는 생성자 (DB 조회 없이)
    public CustomUserDetails(Long id, String email, Collection<? extends GrantedAuthority> authorities) {
        this.user = null; // 전체 User 엔티티는 로드되지 않음
        this.id = id;
        this.email = email;
        this.password = ""; // 토큰 인증 시 비밀번호는 필요 없음
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
