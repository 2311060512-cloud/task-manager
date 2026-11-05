package com.portfolio.taskmanager.security;

import com.portfolio.taskmanager.entity.User;
import com.portfolio.taskmanager.entity.enums.UserRole;
import com.portfolio.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }

        if (auth.getPrincipal() instanceof User user) {
            return Optional.of(user);
        }

        if (auth.getPrincipal() instanceof UserDetails userDetails) {
            return userRepository.findByUsername(userDetails.getUsername());
        }

        if (auth.getName() != null) {
            return userRepository.findByUsername(auth.getName());
        }

        return Optional.empty();
    }

    public boolean isCurrentUserAdmin() {
        return getCurrentUser()
                .map(u -> u.getRole() == UserRole.ADMIN)
                .orElse(false);
    }

    public boolean isCurrentUserClient() {
        return getCurrentUser()
                .map(u -> u.getRole() == UserRole.CLIENT)
                .orElse(false);
    }
}

