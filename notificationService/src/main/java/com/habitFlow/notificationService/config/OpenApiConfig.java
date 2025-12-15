package com.habitFlow.notificationService.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger UI).
 * It customizes the documentation display, providing service metadata and
 * defining the security scheme required for authenticated endpoints, specifically
 * the service-to-service JWT authentication used by this microservice.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .version("1.0.0")
                        .description("""
                                Internal API documentation for Notification microservice.
                                All endpoints are secured via service-to-service JWT authentication.
                                This Swagger is meant only for internal use (DTO schemas and endpoint reference).
                                """)
                )
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .name(securitySchemeName)
                        )
                );
    }
}