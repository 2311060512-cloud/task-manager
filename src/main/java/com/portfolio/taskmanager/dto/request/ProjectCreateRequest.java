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
@Schema(description = "Yêu cầu tạo mới dự án")
public class ProjectCreateRequest {

    @NotBlank(message = "Tên dự án không được để trống")
    @Size(min = 2, max = 100, message = "Tên dự án phải từ 2 đến 100 ký tự")
    @Schema(description = "Tên dự án", example = "Website Thương mại điện tử")
    private String name;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    @Schema(description = "Mô tả dự án", example = "Dự án xây dựng nền tảng e-commerce tích hợp thanh toán")
    private String description;
}

