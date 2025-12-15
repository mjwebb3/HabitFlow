package com.habitFlow.notificationService.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Main Spring Security configuration class for the Notification Service.
 * Defines the security filter chain, authorization rules (which endpoints are internal
 * service-only or public), and inserts the custom JWT filter for service authentication.
 * This setup uses a stateless approach based on service-to-service JWT verification.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ServiceJwtFilter serviceJwtFilter;

    /**
     * Configures the main security filter chain with specific access rules.
     * 1. Disables CSRF (common for stateless microservices).
     * 2. Protects the internal /notifications/** API via ROLE_SERVICE.
     * 3. Permits public access to Swagger/OpenAPI documentation.
     * 4. Denies all other requests by default.
     * 5. Inserts the custom {@link ServiceJwtFilter} for authentication.
     *
     * @param http The HttpSecurity object to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception If configuration fails.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Internal API requires ROLE_SERVICE (validated by ServiceJwtFilter)
                        .requestMatchers("/notifications/**").hasRole("SERVICE")
                        // Swagger/OpenAPI documentation is public
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/api-docs/**"
                        ).permitAll()
                        // Deny all other unknown requests
                        .anyRequest().denyAll()
                )
                .exceptionHandling(ex -> ex
                        // Custom handler for 403 Access Denied errors
                        .accessDeniedHandler(accessDeniedHandler())
                )
                // Insert the custom filter to handle service token authentication
                .addFilterBefore(serviceJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Provides a custom AccessDeniedHandler to format 403 Forbidden responses as JSON,
     * ensuring consistent error handling across the service.
     *
     * @return The configured AccessDeniedHandler.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("""
                { "error": "Missing ROLE_SERVICE authority" }
            """);
        };
    }

}