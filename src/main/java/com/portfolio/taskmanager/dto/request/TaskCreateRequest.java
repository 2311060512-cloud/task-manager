package com.portfolio.taskmanager.dto.request;

import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu tạo mới công việc")
public class TaskCreateRequest {

    @NotBlank(message = "Tiêu đề công việc không được để trống")
    @Size(min = 2, max = 200, message = "Tiêu đề công việc phải từ 2 đến 200 ký tự")
    @Schema(description = "Tiêu đề công việc", example = "Thiết kế database cho module giỏ hàng")
    private String title;

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    @Schema(description = "Mô tả chi tiết công việc", example = "Tạo các bảng cart, cart_items và liên kết khóa ngoại")
    private String description;

    @Schema(description = "Hạn chót hoàn thành (ISO DateTime)", example = "2026-12-31T18:00:00")
    private LocalDateTime dueDate;

    @Schema(description = "Mức độ ưu tiên", example = "HIGH")
    private TaskPriority priority;

    @Schema(description = "Trạng thái công việc", example = "TODO")
    private TaskStatus status;

    @Schema(description = "ID dự án trực thuộc (tùy chọn, mặc định gán vào dự án đầu tiên)", example = "1")
    private Long projectId;

    @Schema(description = "Danh sách tiêu đề công việc con ban đầu (tùy chọn)")
    private List<String> subtasks;
}

