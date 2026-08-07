package com.example.timetablerecommender.auth.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record SessionUser(Long userId, String email, String name) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final List<GrantedAuthority> AUTHORITIES =
            List.of(new SimpleGrantedAuthority("ROLE_USER"));

    public List<GrantedAuthority> authorities() {
        return AUTHORITIES;
    }
}
