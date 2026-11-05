package com.portfolio.taskmanager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.taskmanager.security.ratelimit.LoginRateLimitFilter;
import com.portfolio.taskmanager.security.ratelimit.LoginRateLimiterService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;

    @Autowired(required = false)
    private LoginRateLimiterService rateLimiterService;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean isDev = environment.acceptsProfiles(Profiles.of("dev"));

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"status\":401,\"error\":\"Chưa xác thực (Unauthorized)\",\"message\":\"Vui lòng đăng nhập để truy cập tài nguyên này\",\"path\":\"" + request.getRequestURI() + "\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"status\":403,\"error\":\"Từ chối truy cập (Forbidden)\",\"message\":\"Bạn không có quyền thực hiện thao tác này\",\"path\":\"" + request.getRequestURI() + "\"}");
                        })
                )
                .authorizeHttpRequests(auth -> {
                    auth
                            // Public Static Web & Swagger
                            .requestMatchers("/", "/index.html", "/favicon.ico", "/*.css", "/*.js", "/*.png").permitAll()
                            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                            // Public Auth APIs
                            .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll();

                    // H2 Console CHỈ được mở khi đang chạy profile 'dev'
                    if (isDev) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }

                    // Toàn bộ các API nghiệp vụ khác bắt buộc phải đăng nhập
                    auth.anyRequest().authenticated();
                });

        if (isDev) {
            http.headers(headers -> headers.frameOptions(frame -> frame.disable())); // Cho phép iframe của H2 Console trong Dev
        } else {
            http.headers(headers -> headers.frameOptions(frame -> frame.deny())); // Chặn Clickjacking triệt để trong Prod
        }

        // Đăng ký Filter Rate Limit trước UsernamePasswordAuthenticationFilter khi có service
        if (rateLimiterService != null) {
            ObjectMapper mapper = (objectMapper != null) ? objectMapper : new ObjectMapper();
            http.addFilterBefore(new LoginRateLimitFilter(rateLimiterService, mapper), UsernamePasswordAuthenticationFilter.class);
        }

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
