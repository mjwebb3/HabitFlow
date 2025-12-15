package com.habitFlow.habitService.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Component responsible for generating and providing a signed internal JWT (JSON Web Token).
 * This token is required by the {@link UserService} client to securely authenticate
 * when calling internal REST endpoints on other microservices (fetching user data).
 */
@Component
@RequiredArgsConstructor
public class ServiceTokenProvider {

    private final JwtUtil jwtUtil;

    public String getServiceToken()
    {
        return jwtUtil.generateServiceToken("habit-service");
    }
}