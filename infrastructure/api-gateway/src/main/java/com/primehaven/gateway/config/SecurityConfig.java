package com.primehaven.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Reactive Spring Security configuration for the API Gateway.
 *
 * <p>Purpose: Override the default {@link SecurityWebFilterChain} that
 * Spring Security auto-configures when {@code spring-boot-starter-security}
 * and {@code spring-boot-starter-oauth2-resource-server} are on the classpath.
 * Without this bean the auto-configured chain enables CSRF protection and
 * requires every request to be authenticated, which causes
 * {@code 403 Forbidden} on unauthenticated POST requests such as
 * {@code POST /api/auth/register} — even before the custom
 * {@link com.primehaven.gateway.filter.JwtAuthenticationFilter} executes.
 *
 * <p>JWT validation responsibility stays entirely with
 * {@link com.primehaven.gateway.filter.JwtAuthenticationFilter} (order -100).
 * This configuration only suppresses the conflicting Spring Security defaults;
 * it does NOT add a second authentication mechanism.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Defines the reactive security filter chain.
     *
     * <ul>
     *   <li>CSRF disabled — stateless JWT API; no browser session cookies.</li>
     *   <li>HTTP Basic / form-login disabled — not applicable to a gateway.</li>
     *   <li>OAuth2 resource-server JWT disabled — JWT is validated by the
     *       custom {@code JwtAuthenticationFilter} GlobalFilter, not by Spring
     *       Security's built-in JWT decoder.</li>
     *   <li>Public paths permitted without authentication so that the custom
     *       filter's own public-path logic is never pre-empted by Spring
     *       Security's authorization layer.</li>
     *   <li>All other paths are permitted at the Spring Security layer; the
     *       custom filter enforces JWT presence and validity for protected
     *       routes.</li>
     * </ul>
     *
     * @param http the reactive {@link ServerHttpSecurity} to configure
     * @return the assembled {@link SecurityWebFilterChain}
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            // ----------------------------------------------------------------
            // CSRF — disabled: stateless REST API using JWT bearer tokens.
            // Browser CSRF tokens are only meaningful for cookie-based sessions.
            // ----------------------------------------------------------------
            .csrf(ServerHttpSecurity.CsrfSpec::disable)

            // ----------------------------------------------------------------
            // HTTP Basic & form login — not used by this gateway.
            // ----------------------------------------------------------------
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

            // ----------------------------------------------------------------
            // Authorization rules — mirrors the public-path list declared in
            // JwtAuthenticationFilter so that Spring Security does not block
            // a request before the custom filter can inspect it.
            // ----------------------------------------------------------------
            .authorizeExchange(exchanges -> exchanges
                // Auth endpoints: always public (register / login / validate)
                .pathMatchers("/api/auth/**").permitAll()

                // Actuator endpoints: permit for operational monitoring
                .pathMatchers("/actuator/**").permitAll()

                // Gateway health: permit
                .pathMatchers("/api/gateway/health").permitAll()

                // Property browsing: GET is public per gateway design
                .pathMatchers(HttpMethod.GET, "/api/properties/**").permitAll()

                // Everything else: permitted here so the custom GlobalFilter
                // can return a structured 401 JSON response (instead of a
                // Spring Security 403 HTML page) when the JWT is missing or
                // invalid on protected routes.
                .anyExchange().permitAll()
            );

        return http.build();
    }
}
