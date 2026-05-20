package com.example.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerMonitoringConfig {

    @Bean
    public HealthIndicator circuitBreakerHealthIndicator(CircuitBreakerRegistry circuitBreakerRegistry) {
        return () -> {
            Health.Builder builder = Health.up();
            
            circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
                CircuitBreaker.State state = circuitBreaker.getState();
                String name = circuitBreaker.getName();
                
                builder.withDetail(name + "_state", state.toString());
                builder.withDetail(name + "_failure_rate", 
                    circuitBreaker.getMetrics().getFailureRate());
                builder.withDetail(name + "_calls", 
                    circuitBreaker.getMetrics().getNumberOfBufferedCalls());
                
                if (state == CircuitBreaker.State.OPEN || state == CircuitBreaker.State.FORCED_OPEN) {
                    builder.down();
                }
            });
            
            return builder.build();
        };
    }
}