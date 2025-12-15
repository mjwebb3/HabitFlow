package com.habitFlow.userService.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main config class for Spring Security.
 * Defines the security filter chain, authorization rules (which endpoints are public,
 * authenticated, or internal service-only), password encoding mechanism, and JWT filter order.
 * This setup uses a stateless approach based on JWT authentication.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ServiceJwtFilter serviceJwtFilter;
    private final UserJwtFilter userJwtFilter;

    /**
     * Configures the main security filter chain with specific access rules and custom filters.
     * The configuration enforces the following: CSRF is disabled; Authentication is stateless (implied by JWT usage);
     * Access is defined for Public, Internal Service, and Authenticated User endpoints. The filter order is set
     * where the custom {@link ServiceJwtFilter} runs first for S2S calls, followed by the custom {@link UserJwtFilter}
     * for regular user calls.
     *
     * @param http The HttpSecurity object to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception If configuration fails.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no authentication/token required)
                        .requestMatchers("/auth/register", "/auth/login",
                                "/auth/refresh","/auth/verify/**").permitAll()
                        // Internal service endpoints (requires ROLE_SERVICE authority)
                         .requestMatchers("/auth/internal/**").hasRole("SERVICE")
                        // User authenticated endpoints (requires a valid user JWT)
                        .requestMatchers("/user/me","/user/notification-channel",
                        "/user/regenerate-tg-token","/user/deleteMyData","/auth/logout").authenticated()
                        // Microservice discovery point (public)
                        .requestMatchers("/eureka/**").permitAll()
                        // documentation public endpoints
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/api-docs/**").permitAll()
                        .anyRequest().denyAll()
                )

                // Set up the custom JWT filter chain order:
                // 1. ServiceJwtFilter runs first to handle internal S2S calls.
                // 2. UserJwtFilter runs after ServiceJwtFilter to handle regular user calls.
                // Placing filters here ensures they run before the standard Spring Security filters.
                .addFilterBefore(serviceJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(userJwtFilter, ServiceJwtFilter.class);
        return http.build();
    }

    /**
     * Defines the password encoder used for securely hashing user passwords.
     *
     * @return The BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Custom AccessDeniedHandler for handling 403 Forbidden errors when an authenticated
     * principal tries to access a restricted resource (specifically targeting
     * the internal endpoints where ROLE_SERVICE is expected).
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

    /**
     * Exposes the AuthenticationManager required for handling manual authentication
     * processes (e.g., in a custom login endpoint, although often implicit in JWT-based setups).
     *
     * @param config The AuthenticationConfiguration provided by Spring.
     * @return The configured AuthenticationManager.
     * @throws Exception If configuration fails.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}