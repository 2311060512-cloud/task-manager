package com.portfolio.taskmanager.security;

import com.portfolio.taskmanager.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String DEV_FALLBACK_SECRET = "dev-default-secret-key-must-be-at-least-32-characters-for-hmac-sha";

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration-ms:${jwt.expiration-ms:900000}}")
    private long jwtExpirationMs;

    @Autowired(required = false)
    private Environment environment;

    @PostConstruct
    public void validateConfiguration() {
        if (environment != null && environment.acceptsProfiles(Profiles.of("prod"))) {
            if (!StringUtils.hasText(jwtSecret) || jwtSecret.trim().length() < 32) {
                throw new IllegalStateException(
                        "LỖI BẢO MẬT NGHIÊM TRỌNG: Biến môi trường JWT_SECRET bắt buộc phải được thiết lập và có độ dài tối thiểu 32 ký tự (256-bit) khi chạy profile 'prod'."
                );
            }
        } else {
            if (!StringUtils.hasText(jwtSecret) || jwtSecret.contains("dev-default")) {
                log.warn("CẢNH BÁO AN NINH: Đang sử dụng JWT Secret mặc định cho môi trường DEV. Tuyệt đối không sử dụng giá trị này trên Production!");
            }
        }
    }

    private SecretKey getSigningKey() {
        String effectiveSecret = StringUtils.hasText(jwtSecret) ? jwtSecret.trim() : DEV_FALLBACK_SECRET;
        byte[] keyBytes = effectiveSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("id", userPrincipal.getId())
                .claim("role", userPrincipal.getRole().name())
                .claim("fullName", userPrincipal.getFullName())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("JWT Token không hợp lệ: {}", ex.getMessage());
        }
        return false;
    }
}
