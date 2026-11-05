package com.portfolio.taskmanager.controller;

import com.portfolio.taskmanager.dto.request.QuickTaskRequest;
import com.portfolio.taskmanager.dto.request.TaskCompleteRequest;
import com.portfolio.taskmanager.dto.request.TaskCreateRequest;
import com.portfolio.taskmanager.dto.request.TaskUpdateRequest;
import com.portfolio.taskmanager.dto.response.ApiResponse;
import com.portfolio.taskmanager.dto.response.PageResponse;
import com.portfolio.taskmanager.dto.response.TaskResponse;
import com.portfolio.taskmanager.dto.response.TaskScoreResponse;
import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "APIs quản lý Công việc")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo mới công việc", description = "Tạo một công việc thuộc dự án, có thể kèm danh sách công việc con ban đầu")
    public ApiResponse<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ApiResponse.created("Tạo công việc thành công", response);
    }

    @GetMapping
    @Operation(summary = "Danh sách công việc (Phân trang, lọc & sắp xếp)",
            description = "Hỗ trợ lọc theo trạng thái, mức độ ưu tiên, dự án, khoảng hạn chót, từ khóa, người thực hiện (assigneeId) và mốc thời gian (24h, 3d, 7d, 30d)")
    public ApiResponse<PageResponse<TaskResponse>> getTasks(
            @Parameter(description = "Trạng thái công việc") @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Mức độ ưu tiên") @RequestParam(required = false) TaskPriority priority,
            @Parameter(description = "ID dự án") @RequestParam(required = false) Long projectId,
            @Parameter(description = "Hạn chót từ ngày (ISO DateTime)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDateFrom,
            @Parameter(description = "Hạn chót đến ngày (ISO DateTime)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDateTo,
            @Parameter(description = "Từ khóa tìm kiếm trong tiêu đề hoặc mô tả") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID người được giao việc (-1: Chưa giao, >0: ID nhân sự)") @RequestParam(required = false) Long assigneeId,
            @Parameter(description = "Khoảng thời gian cần lọc (24h, 3d, 7d, 30d)") @RequestParam(required = false) String timeRange,
            @Parameter(description = "Trường thời gian áp dụng lọc (createdAt, updatedAt, dueDate, completedAt, active)") @RequestParam(required = false, defaultValue = "active") String timeField,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số phần tử mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp (dueDate, priority, createdAt, title)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Chiều sắp xếp (ASC, DESC)") @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<TaskResponse> response = taskService.getTasks(status, priority, projectId, dueDateFrom, dueDateTo, keyword, assigneeId, timeRange, timeField, pageable);
        return ApiResponse.success("Lấy danh sách công việc thành công", response);
    }

    @GetMapping("/my-tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Xem danh sách việc được giao cho cá nhân", description = "Quyền: ADMIN hoặc EMPLOYEE")
    public ApiResponse<PageResponse<TaskResponse>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<TaskResponse> response = taskService.getMyTasks(pageable);
        return ApiResponse.success("Lấy danh sách công việc của tôi thành công", response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết công việc", description = "Trả về chi tiết công việc, tiến độ tính theo subtasks và trạng thái quá hạn")
    public ApiResponse<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse response = taskService.getTaskById(id);
        return ApiResponse.success("Lấy chi tiết công việc thành công", response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Cập nhật công việc", description = "Quyền: ADMIN hoặc EMPLOYEE")
    public ApiResponse<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ApiResponse.success("Cập nhật công việc thành công", response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa công việc", description = "Quyền: Chỉ ADMIN có quyền xóa công việc")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Giao việc cho nhân sự hoặc tự nhận việc", description = "Admin có thể giao cho bất kỳ ai, Nhân viên có thể tự nhận việc")
    public ApiResponse<TaskResponse> assignTask(
            @PathVariable Long id,
            @Parameter(description = "ID người được giao việc (để trống nếu tự nhận việc)") @RequestParam(required = false) Long userId) {
        TaskResponse response = taskService.assignTask(id, userId);
        return ApiResponse.success("Phân công công việc thành công", response);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Đánh dấu công việc hoàn thành kèm minh chứng",
            description = "Bắt buộc phải có nội dung minh chứng (ghi chú giải pháp, kết quả kiểm thử, link PR/tài liệu). Quyền: ADMIN hoặc EMPLOYEE")
    public ApiResponse<TaskResponse> markTaskComplete(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) TaskCompleteRequest request) {
        TaskResponse response = taskService.markTaskComplete(id, request);
        return ApiResponse.success("Đã hoàn thành công việc kèm minh chứng thành công", response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    @Operation(summary = "Cập nhật nhanh trạng thái công việc", description = "Quyền: ADMIN, EMPLOYEE hoặc CLIENT (khi yêu cầu sửa lại REWORK)")
    public ApiResponse<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @Parameter(description = "Trạng thái mới (TODO, IN_PROGRESS, REWORK, DONE)") @RequestParam TaskStatus status,
            @Parameter(description = "Ghi chú phản hồi lỗi nếu chuyển sang REWORK") @RequestParam(required = false) String note) {
        TaskResponse response = (note != null && !note.isBlank())
                ? taskService.updateTaskStatus(id, status, note)
                : taskService.updateTaskStatus(id, status);
        return ApiResponse.success("Cập nhật trạng thái công việc thành công", response);
    }

    @GetMapping("/smart-priority")
    @Operation(summary = "Danh sách công việc sắp xếp theo Điểm Ưu Tiên Thông Minh",
            description = "Sắp xếp theo thuật toán đánh giá trọng số mức ưu tiên, số ngày còn lại đến hạn (quá hạn điểm cao nhất) và trạng thái dở dang")
    public ApiResponse<List<TaskScoreResponse>> getSmartPriorityTasks() {
        List<TaskScoreResponse> response = taskService.getSmartPriorityTasks();
        return ApiResponse.success("Lấy danh sách công việc ưu tiên thông minh thành công", response);
    }

    @PostMapping("/quick-parse")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo nhanh công việc bằng ngôn ngữ tự nhiên",
            description = "Phân tích câu nói tiếng Việt/Anh để tự động bóc tách tiêu đề, ngày giờ và mức độ ưu tiên")
    public ApiResponse<TaskResponse> createQuickTask(@Valid @RequestBody QuickTaskRequest request) {
        TaskResponse response = taskService.createQuickTask(request);
        return ApiResponse.created("Tạo công việc từ câu tự nhiên thành công", response);
    }
}

