package com.portfolio.taskmanager.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.taskmanager.dto.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Filter kiểm soát tần suất thử đăng nhập sai, chống brute-force mật khẩu.
 * Được cấu hình trực tiếp trong SecurityConfig, không đánh dấu @Component để tránh
 * đăng ký trùng lặp tại servlet container và không gây ảnh hưởng đến @WebMvcTest slices.
 */
@Slf4j
@RequiredArgsConstructor
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final LoginRateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Chỉ kiểm soát trên endpoint POST /api/v1/auth/login
        if ("POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().endsWith("/api/v1/auth/login")) {
            String clientIp = rateLimiterService.extractClientIp(request);

            if (rateLimiterService.isBlocked(clientIp)) {
                log.warn("Chặn truy cập do vượt quá số lần thử đăng nhập từ IP: {}", clientIp);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");

                ErrorResponse errorResponse = ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.TOO_MANY_REQUESTS.value())
                        .error("Quá nhiều yêu cầu (Too Many Requests)")
                        .message(String.format("Bạn đã đăng nhập sai quá %d lần cho phép. Vui lòng thử lại sau %d giây.",
                                rateLimiterService.getMaxAttempts(), rateLimiterService.getWindowSeconds()))
                        .path(request.getRequestURI())
                        .build();

                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                return;
            }

            try {
                filterChain.doFilter(request, response);

                // Dựa trên HTTP status code từ response để nhận biết đăng nhập thành công hay thất bại
                int status = response.getStatus();
                if (status >= 200 && status < 300) {
                    rateLimiterService.recordSuccess(clientIp);
                } else if (status == HttpServletResponse.SC_UNAUTHORIZED || status == HttpServletResponse.SC_BAD_REQUEST) {
                    rateLimiterService.recordFailure(clientIp);
                }
            } catch (Exception ex) {
                rateLimiterService.recordFailure(clientIp);
                throw ex;
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
