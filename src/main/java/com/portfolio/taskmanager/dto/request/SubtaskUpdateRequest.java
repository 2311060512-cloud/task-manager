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
@Schema(description = "Yêu cầu cập nhật subtask")
public class SubtaskUpdateRequest {

    @NotBlank(message = "Tiêu đề công việc con không được để trống")
    @Size(min = 2, max = 200, message = "Tiêu đề công việc con phải từ 2 đến 200 ký tự")
    @Schema(description = "Tiêu đề công việc con mới", example = "Viết test cho migration script")
    private String title;
}

