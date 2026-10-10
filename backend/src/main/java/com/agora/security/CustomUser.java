package com.agora.security;

import lombok.Getter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

@Getter
public class CustomUser extends User {
    private final UUID id;
    private final String fullName;
    private final String avatar;
    private final String role;

    public CustomUser(
            UUID id,
            String username,
            String password,
            String fullName,
            String avatar,
            String role,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.fullName = fullName;
        this.avatar = avatar;
        this.role = role;
    }
}
