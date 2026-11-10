package com.primehaven.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Gateway filter for JWT token validation and role propagation.
 * Validates HMAC-SHA256 tokens on secured routes and forwards
 * authenticated user identity to downstream microservices via X-User headers.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final SecretKey secretKey;

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/auth/",
            "/actuator/",
            "/api/gateway/health"
    );

    public JwtAuthenticationFilter(
            @Value("${jwt.secret:PrimeHavenDevSecretKeyForJwtSigningMustBeAtLeast256BitsLong2026!}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest originalRequest = exchange.getRequest();
        String path = originalRequest.getURI().getPath();
        HttpMethod method = originalRequest.getMethod();

        // 1. STRIP incoming identity headers to prevent spoofing from external clients
        ServerHttpRequest safeRequest = originalRequest.mutate()
                .headers(headers -> {
                    headers.remove("X-User-Username");
                    headers.remove("X-User-Role");
                })
                .build();
        
        ServerWebExchange safeExchange = exchange.mutate().request(safeRequest).build();

        // 2. Allow public endpoints using the sanitized exchange
        if (isPublic(path, method)) {
            return chain.filter(safeExchange);
        }

        // 3. Validate Authorization header
        String authHeader = safeRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(safeExchange, "Missing or invalid Authorization header. Expected: Bearer <token>", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7).trim();

        // 4. Verify JWT signature and claims
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            // 5. Propagate authenticated user identity downstream
            ServerHttpRequest authenticatedRequest = safeRequest.mutate()
                    .header("X-User-Username", username != null ? username : "")
                    .header("X-User-Role", role != null ? role : "")
                    .build();

            return chain.filter(safeExchange.mutate().request(authenticatedRequest).build());

        } catch (JwtException | IllegalArgumentException e) {
            return onError(safeExchange, "Invalid or expired JWT token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublic(String path, HttpMethod method) {
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        // Public browsing: GET /api/properties (excluding internal availability check)
        return HttpMethod.GET.equals(method) && path.startsWith("/api/properties") && !path.contains("/availability");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Sanitize message to prevent JSON injection if the exception message contains quotes
        String safeMessage = message != null ? message.replace("\"", "\\\"") : "";

        String json = String.format("{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), safeMessage);

        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // Execute early before downstream proxy routing
    }
}