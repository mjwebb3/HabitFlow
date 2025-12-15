package com.habitFlow.userService.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Spring Security Filter responsible for processing and validating the JWT
 * provided in the Authorization header for every incoming HTTP request.
 * If the token is valid and unexpired, the filter extracts the username and sets up
 * the {@link UsernamePasswordAuthenticationToken} in the {@link SecurityContextHolder},
 * allowing access to secured endpoints.
 */
@Component
@RequiredArgsConstructor
public class UserJwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * Performs the authentication logic for incoming requests.
     * If the URI should not be filtered (e.g., /auth/login), the method skips processing.
     * Otherwise, it attempts to validate the Bearer token and set the security context.
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
        System.out.println("[UserJwtFilter] URI: " + request.getRequestURI());
        System.out.println("[UserJwtFilter] Before processing: " + SecurityContextHolder.
                getContext().getAuthentication());

        // Check if the request URI should be excluded
        if (shouldNotFilter(request)) {
            System.out.println("[UserJwtFilter] Skipping internal endpoint");
            filterChain.doFilter(request, response);
            return;
        }
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // bearer prefix check
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication required\"}");
            return;
        }

            String token = authHeader.substring(7);

            try {
                String username = jwtUtil.extractUsername(token);

                // Validate token structure and integrity
                if (!jwtUtil.isTokenValid(token, username)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Token invalid or expired\"}");
                    return;
                }

                // If valid, set authentication context for the request lifecycle
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(username, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);

                // Continue to the next filter in the chain
                filterChain.doFilter(request, response);

                System.out.println("[UserJwtFilter] After filterChain.doFilter: " +
                SecurityContextHolder.getContext().getAuthentication());
                System.out.println("[UserJwtFilter] Authentication set for user: " + username);
            } catch (Exception e) {

                // Handle exceptions during JWT parsing
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{ \"error\": \"JWT error: " + e.getMessage() + "\" }");
                return;
            }

    }

    /**
     * Determines whether the JWT filter should be applied to the given request URI.
     * Authentication endpoints (login, register, refresh, verify) and internal
     * service endpoints must be skipped as they handle authentication differently
     * or are unsecured by design.
     *
     * @param request The HTTP request.
     * @return true if the filter should skip execution for this request, false otherwise.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/auth/register")
                || uri.startsWith("/auth/login")
                || uri.startsWith("/auth/refresh")
                || uri.startsWith("/auth/verify")
                || uri.startsWith("/auth/internal/");
    }
}