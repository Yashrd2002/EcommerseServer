package com.example.gateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtHeaderPropagationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        
    	String path = exchange.getRequest().getURI().getPath();

        // Skip JWT validation for public endpoints
        if (path.startsWith("/auth/secure/login") ||
            path.startsWith("/auth/secure/register") ||
            path.startsWith("/auth/secure/verify-otp") ||
            path.startsWith("/products/public") ||
            path.startsWith("/inventory/public")||
            path.startsWith("/wallet/stripe/") ||
            path.startsWith("/auth/seller/requests") ||
            path.startsWith("/auth/admin/seller-requests")) {
            return chain.filter(exchange);
        }
    	String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // If no Authorization header, skip header propagation
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        
        try {
            String token = authHeader.substring(7);

            // Validate JWT and get claims
            Claims claims = jwtUtil.validateAndGetClaims(token);

            // Extract user info
            Long userId = claims.get("userId", Long.class);
            String username = jwtUtil.extractUsername(claims);
            String role = jwtUtil.extractRole(claims);

            // Mutate request to add headers for downstream microservices
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Username", username)
                    .header("X-User-Role", role)
                    .build();

            // Continue the filter chain with the mutated request
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (RuntimeException e) {
            // Invalid or expired JWT
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired JWT token."));
        }
    }

    @Override
    public int getOrder() {
        return -1; // Run early in the filter chain
    }
}
