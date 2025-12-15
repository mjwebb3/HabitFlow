package com.habitFlow.notificationService.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Spring Security Filter dedicated to validating service-to-service JWTs
 * for internal API endpoints (i.e., requests to /notifications/**).
 * This filter verifies if the token originates from a recognized microservice
 * (HABIT-SERVICE or USER-SERVICE). If the service token is valid,
 * it grants the ROLE_SERVICE authority, required to access the internal APIs.
 */
@Component
@RequiredArgsConstructor
public class ServiceJwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * Extracts the Bearer token, validates it against known service issuers (HABIT-SERVICE, USER-SERVICE),
     * and sets the authenticated context if validation succeeds. If the token is missing or invalid,
     * it immediately terminates the request with a 403 Forbidden error response.
     *
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param filterChain The filter chain.
     * @throws ServletException If a servlet exception occurs.
     * @throws IOException If an I/O exception occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        System.out.println("[ServiceJwtFilter] Authorization header: " + request.getHeader("Authorization"));

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Validate token for HABIT-SERVICE
            if (jwtUtil.isServiceToken(token, "HABIT-SERVICE")) {
                System.out.println("[ServiceJwtFilter] Valid token from HABIT-SERVICE");

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                "HABIT-SERVICE",
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }

            // Validate token for USER-SERVICE
            else if (jwtUtil.isServiceToken(token, "USER-SERVICE")) {
                System.out.println("[ServiceJwtFilter] Valid token from USER-SERVICE");

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                "USER-SERVICE",
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
            else {
                System.out.println("[ServiceJwtFilter] Invalid service token");
            }
        }

        // Final explicit check for 403 response if authentication failed
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("""
        { "error": "Missing ROLE_SERVICE authority" }
    """);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isServiceToken(token, "HABIT-SERVICE") && !jwtUtil.isServiceToken(token, "USER-SERVICE")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("""
        { "error": "Missing ROLE_SERVICE authority" }
    """);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Specifies which requests should be filtered by this component.
     * This filter is designed to target only the internal API endpoints,
     * which are secured for service-to-service communication.
     *
     * @param request The current HTTP request.
     * @return true if the URI does NOT start with /notifications/, meaning
     * it should be SKIPPED by this filter (allowing access to Swagger/health checks).
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/notifications/");
    }
}