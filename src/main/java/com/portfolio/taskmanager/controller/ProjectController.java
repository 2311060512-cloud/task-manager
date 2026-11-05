package com.portfolio.taskmanager.controller;

import com.portfolio.taskmanager.dto.request.ProjectCreateRequest;
import com.portfolio.taskmanager.dto.request.ProjectUpdateRequest;
import com.portfolio.taskmanager.dto.response.ApiResponse;
import com.portfolio.taskmanager.dto.response.ProjectDetailResponse;
import com.portfolio.taskmanager.dto.response.ProjectResponse;
import com.portfolio.taskmanager.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "APIs quản lý Dự án")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Tạo dự án mới", description = "Quyền: ADMIN hoặc EMPLOYEE")
    public ApiResponse<ProjectResponse> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ApiResponse.created("Tạo dự án thành công", response);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả dự án", description = "Trả về danh sách dự án kèm tỷ lệ hoàn thành công việc")
    public ApiResponse<List<ProjectResponse>> getAllProjects() {
        List<ProjectResponse> response = projectService.getAllProjects();
        return ApiResponse.success("Lấy danh sách dự án thành công", response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết dự án theo ID", description = "Trả về chi tiết dự án bao gồm danh sách các công việc trực thuộc")
    public ApiResponse<ProjectDetailResponse> getProjectById(@PathVariable Long id) {
        ProjectDetailResponse response = projectService.getProjectById(id);
        return ApiResponse.success("Lấy chi tiết dự án thành công", response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Cập nhật thông tin dự án", description = "Quyền: ADMIN hoặc EMPLOYEE")
    public ApiResponse<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request) {
        ProjectResponse response = projectService.updateProject(id, request);
        return ApiResponse.success("Cập nhật dự án thành công", response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa dự án", description = "Quyền: Chỉ ADMIN có quyền xóa dự án (Cascade)")
    public void deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
    }
}
