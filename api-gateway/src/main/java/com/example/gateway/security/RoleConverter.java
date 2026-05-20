package com.example.gateway.security;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class RoleConverter {
    public Collection<GrantedAuthority> convertRole(String role) {
        // Spring Security expects ROLE_ prefix
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
