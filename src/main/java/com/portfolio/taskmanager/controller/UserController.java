package com.portfolio.taskmanager.controller;

import com.portfolio.taskmanager.dto.response.ApiResponse;
import com.portfolio.taskmanager.dto.response.UserResponse;
import com.portfolio.taskmanager.entity.enums.UserRole;
import com.portfolio.taskmanager.mapper.UserMapper;
import com.portfolio.taskmanager.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "APIs Người Dùng & Nhân Sự Phân Công")
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/assignees")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Lấy danh sách nhân sự có thể giao việc",
            description = "Trả về danh sách tài khoản Quản trị viên và Nhân viên kỹ thuật")
    public ApiResponse<List<UserResponse>> getEligibleAssignees() {
        var users = userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.EMPLOYEE));
        return ApiResponse.success("Lấy danh sách nhân sự thành công", userMapper.toResponseList(users));
    }
}

