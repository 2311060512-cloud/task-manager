package com.portfolio.taskmanager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.taskmanager.dto.request.LoginRequest;
import com.portfolio.taskmanager.security.ratelimit.LoginRateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class LoginRateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoginRateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService.resetAll();
    }

    @Test
    @DisplayName("Đăng nhập sai quá 5 lần liên tiếp trả về HTTP 429 Too Many Requests")
    void loginRateLimiter_ExceedMaxAttempts_Returns429() throws Exception {
        LoginRequest wrongRequest = LoginRequest.builder()
                .username("admin")
                .password("wrongpassword")
                .build();

        String json = objectMapper.writeValueAsString(wrongRequest);

        // 5 lần đầu tiên đăng nhập sai -> nhận 401 Unauthorized
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        // Lần thứ 6 từ cùng IP -> bị chặn rate limit, trả về 429 Too Many Requests
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Quá nhiều yêu cầu (Too Many Requests)"))
                .andExpect(jsonPath("$.message").value("Bạn đã đăng nhập sai quá 5 lần cho phép. Vui lòng thử lại sau 60 giây."));
    }

    @Test
    @DisplayName("Đăng nhập đúng sau khi reset cửa sổ rate limit thì thành công 200 OK")
    void loginSuccess_AfterReset_Returns200() throws Exception {
        LoginRequest correctRequest = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    @DisplayName("Đăng nhập sai 3 lần, đăng nhập đúng 1 lần sẽ reset bộ đếm thất bại")
    void loginSuccess_ResetsFailedCounter() throws Exception {
        LoginRequest wrongRequest = LoginRequest.builder()
                .username("admin")
                .password("wrongpass")
                .build();

        LoginRequest correctRequest = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();

        // 3 lần sai
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongRequest)))
                    .andExpect(status().isUnauthorized());
        }

        // 1 lần đúng -> reset
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctRequest)))
                .andExpect(status().isOk());

        // Tiếp tục 3 lần sai -> chưa chạm ngưỡng 5 lần nên vẫn là 401 chứ không bị 429
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
