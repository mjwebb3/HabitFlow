package com.habitFlow.notificationService.config;

import com.habitFlow.notificationService.dto.UserDto;
import com.habitFlow.notificationService.exception.custom.UserServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * Client component for **synchronous, internal communication** with the **User Service** (via REST).
 * It uses {@link RestTemplate} and a secured, internal service token to perform lookups.
 * The endpoints accessed here are generally restricted to internal microservices only.
 */
@Component
@RequiredArgsConstructor
public class UserService {

    private final RestTemplate restTemplate;
    private final ServiceTokenProvider tokenProvider;

    /**
     * Fetches core user details (ID, email) from the User Service using the username.
     * This is required when dispatching notifications requested by other services (e.g., Habit Service)
     * which only provide a username.
     *
     * @param username The username of the user to fetch.
     * @return The {@link UserDto} containing necessary user information.
     * @throws UserServiceException if the service token is invalid, the User Service
     * returns an error (e.g., 4xx, 5xx), or if the user is not found.
     */
    public UserDto getUserByUsername(String username) {
        String token = tokenProvider.getServiceToken();
        if (token == null || token.isBlank()) {
            throw new UserServiceException("[UserService] Service token is null or empty!");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        String url = "http://USER-SERVICE/auth/internal/username/" + username;

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<UserDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    UserDto.class
            );

            if (response.getBody() == null) {
                throw new UserServiceException("[UserService] User not found: " + username);
            }

            return response.getBody();

        } catch (HttpStatusCodeException ex) {
            throw new UserServiceException("[UserService] Error fetching userId: " + ex.getStatusCode(), ex);
        } catch (Exception e) {
            throw new UserServiceException("[UserService] Internal error fetching userId", e);
        }
    }

    // can be used later
    /**
     * Checks if a user with the given ID exists in the User Service.
     * This is useful for validating events consumed from Kafka (e.g., UserCleanupEvent)
     * or requests from other services.
     *
     * @param userId The ID of the user to check.
     * @return true if the user exists (2xx status), false if 404 is returned.
     * @throws UserServiceException for any other HTTP status error or internal exception.
     */
     public boolean existsById(Long userId) {
        String token = tokenProvider.getServiceToken();
        if (token == null || token.isBlank()) {
            throw new UserServiceException("[UserService] Service token is null or empty!");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        String url = "http://USER-SERVICE/auth/internal/id/" + userId;

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Void.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw new UserServiceException("[UserService] Error checking user existence: " +
                    ex.getStatusCode(), ex);
        } catch (Exception e) {
            throw new UserServiceException("[UserService] Internal error checking user existence", e);
        }
    }
}