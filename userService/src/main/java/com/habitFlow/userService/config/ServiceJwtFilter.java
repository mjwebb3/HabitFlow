package com.habitFlow.userService.config;

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
 * for internal API endpoints (i.e., requests to /auth/internal/).
 * This filter verifies if the token originates from a recognized microservice
 * (like HABIT-SERVICE or NOTIFICATION-SERVICE). If the service token is valid,
 * it grants the ROLE_SERVICE authority, required to access the internal APIs.
 */
@Component
@RequiredArgsConstructor
public class ServiceJwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * Performs the internal service authentication logic.
     * 1. Checks if the request contains a Bearer token.
     * 2. Attempts to validate the token against known service IDs (HABIT-SERVICE, NOTIFICATION-SERVICE).
     * 3. If a service token is validated, sets the principal as the service ID and grants the {@code ROLE_SERVICE}
     * authority.
     * 4. If the token is present but not a recognized service token, it attempts to extract the username,
     * assuming it might be a user token.
     *
     * @param request The servlet request.
     * @param response The servlet response.
     * @param filterChain The filter chain to proceed to.
     * @throws ServletException If a servlet-related error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        System.out.println("[ServiceJwtFilter] URI: " + request.getRequestURI());
        System.out.println("[ServiceJwtFilter] Before set Authentication: " +
                SecurityContextHolder.getContext().getAuthentication());
        System.out.println("[ServiceJwtFilter] AuthHeader: " + authHeader);

        // bearer header check
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("[ServiceJwtFilter] Received token: " + token);

            // Validate  jwt token for HABIT-SERVICE
            if (jwtUtil.isServiceToken(token, "HABIT-SERVICE")) {
                System.out.println("[ServiceJwtFilter] Valid token from HABIT-SERVICE");

                // Grant ROLE_SERVICE authority
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                "HABIT-SERVICE",
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("[ServiceJwtFilter] After set Authentication: " + SecurityContextHolder.getContext().getAuthentication());

            }

            // Validate jwt token for NOTIFICATION-SERVICE
            else if (jwtUtil.isServiceToken(token, "NOTIFICATION-SERVICE")) {
                System.out.println("[ServiceJwtFilter] Valid token from NOTIFICATION-SERVICE");

                // Grant ROLE_SERVICE authority
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                "NOTIFICATION-SERVICE",
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("[ServiceJwtFilter] After set Authentication: " + SecurityContextHolder.getContext().getAuthentication());

            }

            // Default fall-through: If not a recognized service token, treat it as a regular user token
            // (potentially allowing access based on other security rules, though this path might be redundant if
            // UserJwtFilter handles all user tokens)
            else {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                jwtUtil.extractUsername(token),
                                null,
                                List.of()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
        System.out.println("[ServiceJwtFilter] After filterChain.doFilter: " + SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Specifies which requests should be filtered by this component.
     * This filter is designed to target only the internal API endpoints,
     * which are secured for service-to-service communication.
     *
     * @param request The current HTTP request.
     * @return true if the URI does NOT start with /auth/internal/, meaning
     * it should be SKIPPED by this filter (handled by other filters like {@link UserJwtFilter}).
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/auth/internal/");
    }
}