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
@Schema(description = "Yêu cầu nộp minh chứng hoàn thành công việc")
public class TaskCompleteRequest {

    @Schema(description = "Nội dung minh chứng (kết quả thực hiện, giải pháp, báo cáo nghiệm thu)", example = "Đã merge PR #12 và hoàn tất kiểm thử staging")
    @NotBlank(message = "Minh chứng hoàn thành công việc không được để trống (báo cáo kết quả hoặc giải pháp đã làm)")
    @Size(max = 2000, message = "Nội dung minh chứng không được vượt quá 2000 ký tự")
    private String completionNote;

    @Schema(description = "Đường dẫn đính kèm minh chứng hoặc chuỗi ảnh base64", example = "https://github.com/techcorp/ecommerce/pull/12")
    @Size(max = 5000000, message = "Đường dẫn hoặc dữ liệu ảnh minh chứng không được vượt quá 5MB")
    private String proofUrl;
}
