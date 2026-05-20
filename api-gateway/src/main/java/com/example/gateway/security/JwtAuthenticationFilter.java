package com.example.gateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements ServerAuthenticationConverter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<org.springframework.security.core.Authentication> convert(ServerWebExchange exchange) {
        
    	String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/auth/secure/login") ||
            path.startsWith("/auth/secure/register") ||
            path.startsWith("/auth/secure/verify-otp") ||
            path.startsWith("/products/public") ||
            path.startsWith("/inventory/public") ||
            path.startsWith("/auth/seller/requests") ||
            path.startsWith("/auth/admin/seller-requests")) {
            return Mono.empty();
        }
    	String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.empty(); 
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = jwtUtil.validateAndGetClaims(token);

            String username = jwtUtil.extractUsername(claims);
            String role = jwtUtil.extractRole(claims);

            // Build authorities for Spring Security
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);

            return Mono.just(auth);

        } catch (RuntimeException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired JWT token."));
        }
    }
}
