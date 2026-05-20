package com.example.gateway.config;

import com.example.gateway.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        ReactiveAuthenticationManager authManager = authentication -> Mono.just(authentication);
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authManager);
        authenticationWebFilter.setServerAuthenticationConverter(jwtAuthenticationFilter);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new org.springframework.web.cors.CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:4200"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/auth/secure/login", "/auth/secure/register", "/auth/secure/verify-otp", "/products/public/**","/inventory/public/**","/wallet/stripe/**","/auth/seller/requests", "/auth/admin/seller-requests", "/auth/admin/seller-requests/**").permitAll()
                        .pathMatchers("/auth/getUser","/products/wishlist/**").hasAnyRole("USER", "ADMIN", "SELLER")
                        .pathMatchers("/auth/address/**", "/wallet/**","/notifications/**").hasAnyRole("USER", "SELLER","ADMIN")
                        .pathMatchers("/cart/**", "/orders/user/**", "/orders/user/place","/products/user/**","/delivery/user/**").hasAnyRole("USER","SELLER","ADMIN")
                        .pathMatchers("/orders/seller/**", "/products/seller/**","/inventory/seller/**","/delivery/seller/**").hasRole("SELLER")
                        .pathMatchers("/products/admin/**","/delivery/admin/**","/auth/admin/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

}
