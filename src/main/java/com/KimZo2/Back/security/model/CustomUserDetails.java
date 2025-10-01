package com.KimZo2.Back.security.model;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import com.KimZo2.Back.model.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String nickname;
    private final String provider;
    private final String username;
    private final String password;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.provider = user.getProviderId();
        this.username = null;
        this.password = null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
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
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}