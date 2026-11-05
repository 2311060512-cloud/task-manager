package com.portfolio.taskmanager.security.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Quản lý giới hạn tốc độ thử đăng nhập (Login Rate Limiter) theo địa chỉ IP.
 *
 * <p><strong>LƯU Ý THIẾT KẾ VÀ GIỚI HẠN KIẾN TRÚC:</strong></p>
 * <p>Bộ đếm số lần đăng nhập sai được lưu trữ trực tiếp trong bộ nhớ JVM (in-memory)
 * sử dụng {@link ConcurrentHashMap}. Trong mô hình triển khai phân tán với nhiều instances
 * (cluster / nhiều containers đằng sau Load Balancer), bộ nhớ này KHÔNG được đồng bộ
 * giữa các instance nếu không có giải pháp lưu trữ tập trung như Redis.
 * Thiết kế này được lựa chọn để giữ ứng dụng độc lập, nhẹ và không bắt buộc thêm hạ tầng
 * bên ngoài cho môi trường monolithic hoặc demo portfolio.</p>
 */
@Slf4j
@Service
public class LoginRateLimiterService {

    @Getter
    @Value("${security.login-rate-limit.max-attempts:5}")
    private int maxAttempts;

    @Getter
    @Value("${security.login-rate-limit.window-seconds:60}")
    private int windowSeconds;

    private final ConcurrentHashMap<String, AttemptTracker> attemptsMap = new ConcurrentHashMap<>();

    /**
     * Kiểm tra xem địa chỉ IP hiện tại có đang bị chặn (vượt quá số lần thử sai) hay không.
     */
    public boolean isBlocked(String ip) {
        AttemptTracker tracker = attemptsMap.get(ip);
        if (tracker == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        long windowDurationMs = windowSeconds * 1000L;

        if (now - tracker.getStartTime() > windowDurationMs) {
            // Cửa sổ thời gian đã trôi qua, xóa bản ghi cũ
            attemptsMap.remove(ip);
            return false;
        }

        return tracker.getFailureCount().get() >= maxAttempts;
    }

    /**
     * Ghi nhận 1 lần đăng nhập thất bại từ địa chỉ IP.
     */
    public void recordFailure(String ip) {
        long now = System.currentTimeMillis();
        long windowDurationMs = windowSeconds * 1000L;

        attemptsMap.compute(ip, (key, tracker) -> {
            if (tracker == null || (now - tracker.getStartTime() > windowDurationMs)) {
                return new AttemptTracker(new AtomicInteger(1), now);
            } else {
                tracker.getFailureCount().incrementAndGet();
                return tracker;
            }
        });

        AttemptTracker current = attemptsMap.get(ip);
        int currentCount = current != null ? current.getFailureCount().get() : 1;
        log.warn("Cảnh báo: Đăng nhập thất bại lần {}/{} từ IP: {}", currentCount, maxAttempts, ip);
    }

    /**
     * Reset bộ đếm khi đăng nhập thành công.
     */
    public void recordSuccess(String ip) {
        if (attemptsMap.remove(ip) != null) {
            log.info("Đăng nhập thành công, đã reset bộ đếm rate limit cho IP: {}", ip);
        }
    }

    /**
     * Xóa sạch toàn bộ bộ đếm (hỗ trợ cho test).
     */
    public void resetAll() {
        attemptsMap.clear();
    }

    /**
     * Trích xuất địa chỉ IP thực tế của client từ request (hỗ trợ header proxy).
     */
    public String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    @Getter
    private static class AttemptTracker {
        private final AtomicInteger failureCount;
        private final long startTime;

        public AttemptTracker(AtomicInteger failureCount, long startTime) {
            this.failureCount = failureCount;
            this.startTime = startTime;
        }
    }
}
