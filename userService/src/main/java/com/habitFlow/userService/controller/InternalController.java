package com.habitFlow.userService.controller;

import com.habitFlow.userService.dto.UserDto;
import com.habitFlow.userService.service.InternalFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller providing **read-only internal API endpoints for synchronous
 * communication between microservices. Other services use these endpoints
 * to fetch user data from the User Service.
 * All endpoints in this controller require a service token with the ROLE_SERVICE authority.
 */
@RestController
@RequestMapping("/auth/internal")
@RequiredArgsConstructor
@Tag(name = "Internal services", description = "Internal endpoints for services")
@SecurityRequirement(name = "bearerAuth")
public class InternalController {

    private final InternalFacade internalFacade;

    /**
     * Retrieves a user's DTO by their username. Requires ROLE_SERVICE.
     * This is a **data retrieval** operation.
     *
     * @param username The username of the user to fetch.
     * @return ResponseEntity containing the {@link  UserDto}.
     */
    @Operation(summary = "Get user by username (internal)", description = "Used by other services to" +
            " fetch user info by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found and returned successfully"),
            @ApiResponse(responseCode = "403", description = "Missing ROLE_SERVICE authority"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(
            @Parameter(description = "Username of the user", example = "john_doe")
            @PathVariable String username) {
        return internalFacade.getUserByUsername(username);
    }

    /**
     * Checks if a user exists by their ID. Requires ROLE_SERVICE.
     * This is a validation/existence check operation.
     *
     * @param id The ID of the user to check.
     * @return ResponseEntity with status 200 if the user exists.
     */
    @Operation(
            summary = "Check if user exists by ID (internal)",
            description = "Used for validation between services, e.g. before habit creation or review publishing."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found and returned successfully"),
            @ApiResponse(responseCode = "403", description = "Missing ROLE_SERVICE authority"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/id/{id}")
    public ResponseEntity<Void> checkUserExists(
            @Parameter(description = "ID of the user", example = "123") @PathVariable Long id) {
        return internalFacade.checkUserExists(id);
    }

    /**
     * Retrieves multiple user DTOs based on a list of IDs via a POST request. Requires ROLE_SERVICE.
     * This is a **batch data retrieval** operation.
     *
     * @param ids A list of Longs representing the user IDs to retrieve.
     * @return ResponseEntity containing a List of {@link UserDto} objects.
     */
    @Operation(
            summary = "Get multiple users by IDs (internal)",
            description = "Fetches multiple user records in one request. " +
                    "Used by Habit or Notification services when processing batch operations."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of users returned successfully"),
            @ApiResponse(responseCode = "403", description = "Missing ROLE_SERVICE authority")
    })
    @PostMapping("/ids")
    public ResponseEntity<List<UserDto>> getUsersByIds(@RequestBody List<Long> ids) {
        List<UserDto> users = internalFacade.getUsersByIds(ids);
        return ResponseEntity.ok(users);
    }
}
