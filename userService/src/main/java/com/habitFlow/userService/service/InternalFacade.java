package com.habitFlow.userService.service;

import com.habitFlow.userService.dto.UserDto;
import com.habitFlow.userService.exception.custom.ForbiddenException;
import com.habitFlow.userService.exception.custom.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * InternalFacade handles internal service-to-service user operations.
 * It centralizes checks (e.g., ROLE_SERVICE authority) and user retrieval logic
 * for other microservices, ensuring internal API endpoints are secure and functional.
 */
@Service
@RequiredArgsConstructor
public class InternalFacade {

    private final UserService userService;

    /**
     * Retrieves a {@link UserDto} by username. This endpoint requires the calling
     * service to possess the ROLE_SERVICE authority.
     *
     * @param username The username of the requested user.
     * @return ResponseEntity containing the {@link UserDto}.
     * @throws ForbiddenException If the caller lacks ROLE_SERVICE authority.
     * @throws UserNotFoundException If the user does not exist.
     */
    public ResponseEntity<UserDto> getUserByUsername(String username) {
        if (!hasServiceRole()) {
            throw new ForbiddenException("Missing ROLE_SERVICE authority");
        }

        UserDto dto = userService.findUserDtoByUsername(username);
        if (dto == null)  throw new UserNotFoundException("User not found");

        return ResponseEntity.ok(dto);
    }

    /**
     * Checks if a user exists by ID. This operation requires ROLE_SERVICE authority.
     *
     * @param id The ID of the user to check.
     * @return ResponseEntity with status 200 (OK) if the user exists.
     * @throws ForbiddenException If the caller lacks ROLE_SERVICE authority.
     * @throws UserNotFoundException If the user does not exist.
     */
    public ResponseEntity<Void> checkUserExists(Long id) {
        if (!hasServiceRole()) {
            throw new ForbiddenException("Missing ROLE_SERVICE authority");
        }

        boolean exists = userService.existsById(id);
        if (!exists) throw new UserNotFoundException("User not found");

        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves a list of User DTOs based on a list of IDs. Requires ROLE_SERVICE authority.
     *
     * @param ids List of user IDs.
     * @return List of {@link UserDto} objects corresponding to the provided IDs.
     * @throws ForbiddenException If the caller lacks ROLE_SERVICE authority.
     */
    public List<UserDto> getUsersByIds(List<Long> ids) {
        if (!hasServiceRole()) {
            throw new ForbiddenException("Missing ROLE_SERVICE authority");
        }

        return userService.findUsersByIds(ids);
    }

    /**
     * Checks if the current security context possesses the ROLE_SERVICE authority.
     *
     * @return true if ROLE_SERVICE is present, false otherwise.
     */
    private boolean hasServiceRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SERVICE"));
    }
}