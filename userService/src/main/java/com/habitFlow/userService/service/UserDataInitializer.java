package com.habitFlow.userService.service;

import com.habitFlow.userService.model.User;
import com.habitFlow.userService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * db data initialization for manual testing of certain things like {@link CleanupServiceScheduler}
 */
@Component
@RequiredArgsConstructor
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
//    userRepository.deleteAll();
//
//        // Abandoned users (should be deleted cleanupInactiveUsers)
//        for (int i = 1; i <= 4; i++) {
//            User user = User.builder()
//                    .username("inactive_" + i)
//                    .password("password")
//                    .email("inactive_" + i + "@example.com")
//                    .emailVerified(true)
//                    .createdAt(LocalDateTime.now().minusYears(2))
//                    .lastActiveAt(LocalDateTime.now().minusDays(400))
//                    .build();
//            userRepository.save(user);
//        }
//
//        // Unverified users (should be removed cleanupUnverifiedUsers)
//        for (int i = 1; i <= 5; i++) {
//            User user = User.builder()
//                    .username("unverified_" + i)
//                    .password("password")
//                    .email("unverified_" + i + "@example.com")
//                    .emailVerified(false)
//                    .createdAt(LocalDateTime.now().minusDays(3))
//                    .lastActiveAt(LocalDateTime.now().minusDays(3))
//                    .build();
//            userRepository.save(user);
//        }
//
//        // Active users (should not be deleted)
//        for (int i = 1; i <= 3; i++) {
//            User user = User.builder()
//                    .username("active_" + i)
//                    .password("password")
//                    .email("active_" + i + "@example.com")
//                    .emailVerified(true)
//                    .createdAt(LocalDateTime.now().minusDays(10))
//                    .lastActiveAt(LocalDateTime.now())
//                    .build();
//            userRepository.save(user);
//        }
//
//        System.out.println("[UserDataInitializer] Added test users:");
//        System.out.println("→ 4 inactive (for cleanupInactiveUsers)");
//        System.out.println("→ 5 unverified (for cleanupUnverifiedUsers)");
//        System.out.println("→ 3 active (should stay)");

    }
}