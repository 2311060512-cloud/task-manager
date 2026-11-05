package com.portfolio.taskmanager.mapper;

import com.portfolio.taskmanager.dto.response.UserResponse;
import com.portfolio.taskmanager.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .roleDisplayName(user.getRole() != null ? user.getRole().getDisplayName() : null)
                .build();
    }

    public List<UserResponse> toResponseList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream().map(this::toResponse).toList();
    }
}

