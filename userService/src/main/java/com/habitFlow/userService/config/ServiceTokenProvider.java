package com.habitFlow.userService.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Component responsible for generating the internal service-to-service token.
 * This token is used by the User Service itself when it needs to authenticate
 * as a service principal (with the ROLE_SERVICE authority) when making calls
 * to other internal microservices.
 */
@Component
@RequiredArgsConstructor
public class ServiceTokenProvider {

    private final JwtUtil jwtUtil;

    public String getServiceToken()
    {
        return jwtUtil.generateServiceToken("user-service");
    }
}