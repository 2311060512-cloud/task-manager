package com.portfolio.taskmanager.dto.request;

import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu cập nhật công việc")
public class TaskUpdateRequest {

    @NotBlank(message = "Tiêu đề công việc không được để trống")
    @Size(min = 2, max = 200, message = "Tiêu đề công việc phải từ 2 đến 200 ký tự")
    @Schema(description = "Tiêu đề công việc mới", example = "Hoàn thiện API giỏ hàng")
    private String title;

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    @Schema(description = "Mô tả chi tiết mới")
    private String description;

    @Schema(description = "Hạn chót mới", example = "2026-12-31T20:00:00")
    private LocalDateTime dueDate;

    @NotNull(message = "Mức độ ưu tiên không được để trống")
    @Schema(description = "Mức độ ưu tiên", example = "URGENT")
    private TaskPriority priority;

    @NotNull(message = "Trạng thái không được để trống")
    @Schema(description = "Trạng thái công việc", example = "IN_PROGRESS")
    private TaskStatus status;

    @NotNull(message = "ID dự án không được để trống")
    @Schema(description = "ID dự án trực thuộc", example = "1")
    private Long projectId;
}

