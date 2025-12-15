package com.habitFlow.habitService.config;

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
 * Custom Spring Security Filter responsible for processing JWT tokens
 * provided in the Authorization header.
 * This filter intercepts incoming requests, validates the JWT, and if
 * valid, populates the Spring Security Context with the authenticated user's
 * details (username), allowing subsequent security checks to pass.
 * It extends {@link OncePerRequestFilter} to ensure it runs only once per request.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * Performs the authentication logic for incoming requests.
     * 1. Extracts the Bearer token from the Authorization header.
     * 2. If the token is missing or malformed, writes an unauthorized response and halts the chain.
     * 3. Validates the token's signature and extracts the username.
     * 4. If valid, creates an {@link UsernamePasswordAuthenticationToken} and sets it
     * in the {@link SecurityContextHolder}.
     * 5. Proceeds to the next filter in the chain.
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
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // bearer header check
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorizedResponse(response, "Full authentication is required to access this resource");
            return;
        }
            // Extract the token
            String token = authHeader.substring(7);
            try {
                String username = jwtUtil.extractUsername(token);

                // Check if the token is valid
                if (!jwtUtil.isTokenValid(token, username)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(username, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

        filterChain.doFilter(request, response);
    }

    /**
     * Utility method to write a standardized 401 Unauthorized response body
     * with a JSON format.
     *
     * @param response The HttpServletResponse to modify.
     * @param message The error message to include in the response body.
     * @throws IOException If writing to the response stream fails.
     */
    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}