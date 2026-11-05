package com.portfolio.taskmanager.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtSecurityValidationTest {

    @Test
    @DisplayName("Profile prod thiếu JWT_SECRET thì dừng khởi động ứng dụng (fail-fast)")
    void prodProfile_MissingSecret_ThrowsException() {
        Environment env = mock(Environment.class);
        when(env.acceptsProfiles(any(Profiles.class))).thenAnswer(invocation -> {
            Profiles p = invocation.getArgument(0);
            return p.matches(profile -> profile.equals("prod"));
        });

        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "environment", env);
        ReflectionTestUtils.setField(provider, "jwtSecret", "");

        assertThrows(IllegalStateException.class, provider::validateConfiguration);
    }

    @Test
    @DisplayName("Profile prod với JWT_SECRET ngắn hơn 32 ký tự thì dừng khởi động (fail-fast)")
    void prodProfile_ShortSecret_ThrowsException() {
        Environment env = mock(Environment.class);
        when(env.acceptsProfiles(any(Profiles.class))).thenAnswer(invocation -> {
            Profiles p = invocation.getArgument(0);
            return p.matches(profile -> profile.equals("prod"));
        });

        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "environment", env);
        ReflectionTestUtils.setField(provider, "jwtSecret", "short-secret-key-too-short");

        assertThrows(IllegalStateException.class, provider::validateConfiguration);
    }

    @Test
    @DisplayName("Profile prod với JWT_SECRET hợp lệ (>= 32 ký tự) khởi động thành công")
    void prodProfile_ValidSecret_Passes() {
        Environment env = mock(Environment.class);
        when(env.acceptsProfiles(any(Profiles.class))).thenAnswer(invocation -> {
            Profiles p = invocation.getArgument(0);
            return p.matches(profile -> profile.equals("prod"));
        });

        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "environment", env);
        ReflectionTestUtils.setField(provider, "jwtSecret", "production-super-secret-key-that-is-at-least-32-bytes-long");

        assertDoesNotThrow(provider::validateConfiguration);
    }

    @Test
    @DisplayName("Profile dev không truyền JWT_SECRET vẫn khởi động an toàn với key mặc định")
    void devProfile_DefaultSecret_Passes() {
        Environment env = mock(Environment.class);
        when(env.acceptsProfiles(any(Profiles.class))).thenAnswer(invocation -> {
            Profiles p = invocation.getArgument(0);
            return p.matches(profile -> profile.equals("dev"));
        });

        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "environment", env);
        ReflectionTestUtils.setField(provider, "jwtSecret", "dev-default-secret-key-must-be-at-least-32-characters-for-hmac-sha");

        assertDoesNotThrow(provider::validateConfiguration);
    }
}
