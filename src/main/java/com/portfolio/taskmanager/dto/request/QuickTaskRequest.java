package com.portfolio.taskmanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu tạo nhanh công việc bằng ngôn ngữ tự nhiên")
public class QuickTaskRequest {

    @NotBlank(message = "Nội dung câu tự nhiên không được để trống")
    @Schema(description = "Câu mô tả công việc bằng tiếng Việt/tiếng Anh", example = "Họp khách hàng lúc 14:00 ngày mai mức độ URGENT")
    private String text;

    @Schema(description = "ID dự án (nếu để trống hệ thống sẽ tự gán vào dự án mặc định đầu tiên)", example = "1")
    private Long projectId;
}

