package com.habitFlow.userService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habitFlow.userService.config.JwtUtil;
import com.habitFlow.userService.dto.UpdateChannelRequest;
import com.habitFlow.userService.model.User;
import com.habitFlow.userService.repository.UserRepository;
import com.habitFlow.userService.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.habitFlow.Kafka.NotificationChannel;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link UserController}. Check API endpoints
 * for authenticated users (/user/*), including data retrieval,
 * notification channel updates, and account deletion.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserService userService;

    @MockBean
    private NotificationProducer notificationProducer;

    @MockBean
    private UserCleanupProducer cleanupProducer;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;

    private User testUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        testUser = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password(passwordEncoder.encode("1234password"))
                .lastActiveAt(LocalDateTime.now())
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(testUser);

        userToken = jwtUtil.generateAccessToken("john_doe");

        // Kafka mock producers do not perform any actions
        doNothing().when(notificationProducer)
                .sendCreateInitialSettings(any());

        doNothing().when(notificationProducer)
                .sendVerificationEmail(any());

        doNothing().when(notificationProducer)
                .sendConfirmEmailChannel(any());
    }

    // ================= /user/me =================

    @Test
    @DisplayName("✅ 200 - Successfully fetch current user info")
    void getCurrentUserInfo_success() throws Exception {
        mockMvc.perform(get("/user/me")
                        .header("Authorization", "Bearer " + userToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("❌ 401 - Unauthorized (missing token)")
    void getCurrentUserInfo_unauthorized() throws Exception {
        mockMvc.perform(get("/user/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    @DisplayName("❌ 404 - User not found")
    void getCurrentUserInfo_userNotFound() throws Exception {
        userRepository.deleteAll();
        mockMvc.perform(get("/user/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    // ================= /user/notification-channel =================

    @Test
    @DisplayName("✅ 200 - Successfully update notification channel")
    void updateNotificationChannel_success() throws Exception {

        UpdateChannelRequest request = new UpdateChannelRequest();
        request.setChannel(NotificationChannel.TG);

        mockMvc.perform(post("/user/notification-channel")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification channel update requested"));
    }

    @Test
    @DisplayName("❌ 400 - Invalid request (missing channel field)")
    void updateNotificationChannel_invalidRequest() throws Exception {
        UpdateChannelRequest request = new UpdateChannelRequest();

        mockMvc.perform(post("/user/notification-channel")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.channel")
                        .value("Channel cannot be null. Allowed values: EMAIL, TG, NONE."));
    }

    @Test
    @DisplayName("❌ 401 - Unauthorized (missing token)")
    void updateNotificationChannel_unauthorized() throws Exception {
        UpdateChannelRequest request = new UpdateChannelRequest();
        request.setChannel(NotificationChannel.EMAIL);

        mockMvc.perform(post("/user/notification-channel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    @DisplayName("❌ 404 - User not found when updating channel")
    void updateNotificationChannel_userNotFound() throws Exception {
        userRepository.deleteAll();

        UpdateChannelRequest request = new UpdateChannelRequest();
        request.setChannel(NotificationChannel.TG);

        mockMvc.perform(post("/user/notification-channel")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    // ================= /user/regenerate-tg-token =================

    @Test
    @DisplayName("✅ 200 - Telegram token regenerated successfully")
    void regenerateTelegramToken_success() throws Exception {

        mockMvc.perform(post("/user/regenerate-tg-token")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(content().string("A new Telegram token has been sent to your email."));
    }

    @Test
    @DisplayName("❌ 401 - Unauthorized (missing token)")
    void regenerateTelegramToken_unauthorized() throws Exception {
        mockMvc.perform(post("/user/regenerate-tg-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    @DisplayName("❌ 404 - User not found (regenerate token)")
    void regenerateTelegramToken_userNotFound() throws Exception {
        userRepository.deleteAll();
        mockMvc.perform(post("/user/regenerate-tg-token")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    // ================= /user/deleteMyData =================

    @Test
    @DisplayName("✅ 200 - Successfully delete own account")
    void deleteMyData_success() throws Exception {

        mockMvc.perform(delete("/user/deleteMyData")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Your account and all related data were successfully deleted."));

        boolean exists = userRepository.existsById(testUser.getId());
        assert(!exists);
    }

    @Test
    @DisplayName("❌ 401 - Unauthorized (missing token)")
    void deleteMyData_unauthorized() throws Exception {
        mockMvc.perform(delete("/user/deleteMyData"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    @DisplayName("❌ 404 - User not found (delete my data)")
    void deleteMyData_userNotFound() throws Exception {
        userRepository.deleteAll();

        mockMvc.perform(delete("/user/deleteMyData")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }
}