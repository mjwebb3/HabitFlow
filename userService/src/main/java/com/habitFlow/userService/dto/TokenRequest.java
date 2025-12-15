package com.habitFlow.userService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO used to carry the refresh token required to log a user out.
 * The token is sent to the server to be revoked.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request containing a refresh token for operations like refresh or logout")
public class TokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "The refresh token to be revoked", example = "d0d8dfe7-816d-41d2-9fbf-16f02cfecf2e")
    private String refreshToken;
}