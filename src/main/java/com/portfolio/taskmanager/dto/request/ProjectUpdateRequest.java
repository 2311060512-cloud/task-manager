package com.portfolio.taskmanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Yêu cầu cập nhật thông tin dự án")
public class ProjectUpdateRequest {

    @NotBlank(message = "Tên dự án không được để trống")
    @Size(min = 2, max = 100, message = "Tên dự án phải từ 2 đến 100 ký tự")
    @Schema(description = "Tên dự án mới", example = "Website E-Commerce V2")
    private String name;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    @Schema(description = "Mô tả dự án mới", example = "Bản nâng cấp hiệu năng và giao diện")
    private String description;
}

