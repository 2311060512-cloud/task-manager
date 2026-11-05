package com.portfolio.taskmanager.dto.request;

import com.portfolio.taskmanager.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Yêu cầu đăng ký tài khoản")
public class RegisterRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3 đến 50 ký tự")
    @Schema(description = "Tên đăng nhập", example = "khachhang_moi")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải tối thiểu 6 ký tự")
    @Schema(description = "Mật khẩu", example = "123456")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    @Schema(description = "Họ và tên đầy đủ", example = "Nguyễn Văn Khách")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    @Schema(description = "Địa chỉ email", example = "khachhang@congty.com")
    private String email;

    @Schema(description = "Vai trò (ADMIN, EMPLOYEE, CLIENT - mặc định là CLIENT)", example = "CLIENT")
    private UserRole role;
}

