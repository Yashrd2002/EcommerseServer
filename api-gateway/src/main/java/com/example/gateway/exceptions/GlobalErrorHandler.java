package com.example.gateway.exceptions;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Order(-2) // Ensures this handler executes before default ones
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String message;

        // Determine status and message
        if (ex instanceof ResponseStatusException responseStatusException) {
            status = (HttpStatus) responseStatusException.getStatusCode();
            message = responseStatusException.getReason() != null
                    ? responseStatusException.getReason()
                    : status.getReasonPhrase();
        } else if (ex.getMessage() != null && ex.getMessage().contains("JWT")) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Invalid or expired JWT token.";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An unexpected error occurred.";
        }

        // Build error response
        Map<String, Object> errorAttributes = Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", exchange.getRequest().getPath().value()
        );

        // Prepare JSON
        String json;
        try {
            json = objectMapper.writeValueAsString(errorAttributes);
        } catch (Exception e) {
            json = "{\"error\": \"Internal serialization error\"}";
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        return exchange.getResponse()
                .writeWith(Mono.just(bufferFactory.wrap(json.getBytes(StandardCharsets.UTF_8))));
    }
}
