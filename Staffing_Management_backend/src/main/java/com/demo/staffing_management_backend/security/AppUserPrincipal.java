package com.demo.staffing_management_backend.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class AppUserPrincipal implements UserDetails {

    private final String userId;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final int tokenVersion;
    private final Collection<? extends GrantedAuthority> authorities;

    public AppUserPrincipal(String userId, String username, String password, boolean enabled,
                            int tokenVersion, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.tokenVersion = tokenVersion;
        this.authorities = authorities;
    }

    public String getUserId() {
        return userId;
    }

    public int getTokenVersion() {
        return tokenVersion;
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
        return username;
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
        return enabled;
    }
}
