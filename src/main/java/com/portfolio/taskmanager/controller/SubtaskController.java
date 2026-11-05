package com.portfolio.taskmanager.controller;

import com.portfolio.taskmanager.dto.request.SubtaskCreateRequest;
import com.portfolio.taskmanager.dto.request.SubtaskUpdateRequest;
import com.portfolio.taskmanager.dto.response.ApiResponse;
import com.portfolio.taskmanager.dto.response.SubtaskResponse;
import com.portfolio.taskmanager.service.SubtaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
@Tag(name = "Subtasks", description = "APIs quản lý Công việc con (Quyền: ADMIN hoặc EMPLOYEE)")
public class SubtaskController {

    private final SubtaskService subtaskService;

    @PostMapping("/tasks/{taskId}/subtasks")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo công việc con cho Task", description = "Thêm một subtask mới vào task chỉ định")
    public ApiResponse<SubtaskResponse> createSubtask(
            @PathVariable Long taskId,
            @Valid @RequestBody SubtaskCreateRequest request) {
        SubtaskResponse response = subtaskService.createSubtask(taskId, request);
        return ApiResponse.created("Tạo công việc con thành công", response);
    }

    @PatchMapping("/subtasks/{id}/toggle")
    @Operation(summary = "Đổi trạng thái hoàn thành của subtask", description = "Chuyển đổi qua lại giữa đã hoàn thành (true) và chưa hoàn thành (false)")
    public ApiResponse<SubtaskResponse> toggleSubtask(@PathVariable Long id) {
        SubtaskResponse response = subtaskService.toggleSubtask(id);
        return ApiResponse.success("Cập nhật trạng thái công việc con thành công", response);
    }

    @PutMapping("/subtasks/{id}")
    @Operation(summary = "Cập nhật tiêu đề công việc con", description = "Đổi tên/tiêu đề của subtask")
    public ApiResponse<SubtaskResponse> updateSubtask(
            @PathVariable Long id,
            @Valid @RequestBody SubtaskUpdateRequest request) {
        SubtaskResponse response = subtaskService.updateSubtask(id, request);
        return ApiResponse.success("Cập nhật công việc con thành công", response);
    }

    @DeleteMapping("/subtasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Xóa công việc con", description = "Xóa subtask khỏi task")
    public void deleteSubtask(@PathVariable Long id) {
        subtaskService.deleteSubtask(id);
    }
}
