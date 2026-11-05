package com.portfolio.taskmanager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.taskmanager.dto.request.QuickTaskRequest;
import com.portfolio.taskmanager.dto.request.RegisterRequest;
import com.portfolio.taskmanager.dto.request.SubtaskCreateRequest;
import com.portfolio.taskmanager.dto.request.TaskCompleteRequest;
import com.portfolio.taskmanager.dto.request.TaskUpdateRequest;
import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.entity.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class RoleAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Chưa đăng nhập truy cập endpoint bảo mật trả về 401 Unauthorized")
    void unauthenticatedAccess_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("CLIENT cố xóa dự án trả về 403 Forbidden (chỉ ADMIN có quyền)")
    @WithMockUser(username = "client_vinfast", roles = {"CLIENT"})
    void clientDeletingProject_Returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("EMPLOYEE cố xóa dự án trả về 403 Forbidden (chỉ ADMIN có quyền)")
    @WithMockUser(username = "dev_nam", roles = {"EMPLOYEE"})
    void employeeDeletingProject_Returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("CLIENT cố tạo subtask trả về 403 Forbidden (chỉ ADMIN hoặc EMPLOYEE có quyền)")
    @WithMockUser(username = "client_vinfast", roles = {"CLIENT"})
    void clientCreatingSubtask_Returns403() throws Exception {
        SubtaskCreateRequest request = SubtaskCreateRequest.builder()
                .title("Subtask trái phép")
                .build();

        mockMvc.perform(post("/api/v1/tasks/1/subtasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("ADMIN hoặc EMPLOYEE có thể xem danh sách dự án trả về 200 OK")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanAccessProjects_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("EMPLOYEE truyền userId của người khác vào /assign bị chặn 403 Forbidden")
    @WithMockUser(username = "dev_nam", roles = {"EMPLOYEE"})
    void employeeAssigningTaskToAnotherUser_Returns403() throws Exception {
        // dev_nam có id=2, cố tình giao việc cho dev_hoa có id=3 -> phải bị từ chối 403
        mockMvc.perform(patch("/api/v1/tasks/1/assign")
                        .param("userId", "3"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Chỉ Quản trị viên mới có quyền giao việc cho người khác"));
    }

    @Test
    @DisplayName("EMPLOYEE tự nhận việc cho chính mình (không truyền userId hoặc truyền chính id của mình) trả về 200 OK")
    @WithMockUser(username = "dev_nam", roles = {"EMPLOYEE"})
    void employeeAssigningTaskToSelf_Returns200() throws Exception {
        mockMvc.perform(patch("/api/v1/tasks/1/assign"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assigneeName").value("Nguyễn Văn Nam (Tech Lead)"));
    }

    @Test
    @DisplayName("ADMIN có quyền giao việc cho bất kỳ nhân viên nào trả về 200 OK")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanAssignTaskToAnyEmployee_Returns200() throws Exception {
        mockMvc.perform(patch("/api/v1/tasks/1/assign")
                        .param("userId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assigneeName").value("Trần Thị Hoa (Senior Backend)"));
    }

    @Test
    @DisplayName("EMPLOYEE xem chi tiết công việc của người khác bị chặn 403 Forbidden")
    @WithMockUser(username = "dev_hoa", roles = {"EMPLOYEE"})
    void employeeViewingOtherEmployeeTask_Returns403() throws Exception {
        // Task 1 được giao cho dev_nam (id=2), dev_hoa (id=3) không có quyền xem
        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Bạn chỉ có quyền xem chi tiết công việc của chính mình"));
    }

    @Test
    @DisplayName("EMPLOYEE xem chi tiết công việc của chính mình trả về 200 OK")
    @WithMockUser(username = "dev_nam", roles = {"EMPLOYEE"})
    void employeeViewingOwnTask_Returns200() throws Exception {
        // Task 1 được giao cho dev_nam (id=2)
        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("Công việc chưa hoàn thành 100% subtask thì không thể chuyển sang DONE (trả về 400 Bad Request)")
    @WithMockUser(username = "dev_hoa", roles = {"EMPLOYEE"})
    void taskWithIncompleteSubtasks_CannotBeMarkedDone_Returns400() throws Exception {
        // Task 2 có subtask chưa xong
        mockMvc.perform(patch("/api/v1/tasks/2/status")
                        .param("status", "DONE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Không thể chuyển sang trạng thái HOÀN TẤT: Tất cả công việc con (subtasks) phải hoàn thành 100%"));
    }

    @Test
    @DisplayName("CLIENT yêu cầu sửa lại (REWORK) công việc thuộc dự án của mình thành công (200 OK)")
    @WithMockUser(username = "client_vinfast", roles = {"CLIENT"})
    void clientRequestingRework_OnOwnProjectTask_Returns200() throws Exception {
        // Project ecommerce thuộc về client_vinfast, task 1 thuộc project ecommerce
        mockMvc.perform(patch("/api/v1/tasks/1/status")
                        .param("status", "REWORK")
                        .param("note", "Lỗi kiểm thử: cần bổ sung index cho trường email"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REWORK"))
                .andExpect(jsonPath("$.data.reworkNote").value("Lỗi kiểm thử: cần bổ sung index cho trường email"));
    }

    @Test
    @DisplayName("Chưa đăng nhập truy cập /api/v1/auth/me trả về 401 Unauthorized thay vì 500 NPE")
    void unauthenticatedAccessToAuthMe_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Chưa xác thực (Unauthorized)"));
    }

    @Test
    @DisplayName("CLIENT truy cập chi tiết dự án của khách hàng khác (IDOR) bị chặn 403 Forbidden")
    @WithMockUser(username = "client_vinfast", roles = {"CLIENT"})
    void clientAccessingOtherClientProject_Returns403() throws Exception {
        // Dự án 2 thuộc về client_fpt, client_vinfast không có quyền truy cập
        mockMvc.perform(get("/api/v1/projects/2"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Khách hàng chỉ có quyền xem chi tiết dự án của mình"));
    }

    @Test
    @DisplayName("CLIENT truy cập chi tiết dự án của chính mình trả về 200 OK")
    @WithMockUser(username = "client_vinfast", roles = {"CLIENT"})
    void clientAccessingOwnProject_Returns200() throws Exception {
        // Dự án 1 thuộc về client_vinfast
        mockMvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("EMPLOYEE cố tình sửa công việc của nhân viên khác bị chặn 403 Forbidden")
    @WithMockUser(username = "dev_hoa", roles = {"EMPLOYEE"})
    void employeeUpdatingOtherEmployeeTask_Returns403() throws Exception {
        // Task 1 do admin tạo và giao cho dev_nam, dev_hoa không có quyền sửa
        TaskUpdateRequest request = TaskUpdateRequest.builder()
                .title("Tự ý đổi tiêu đề Task 1")
                .description("Mô tả thay đổi trái phép")
                .priority(TaskPriority.LOW)
                .status(TaskStatus.TODO)
                .projectId(1L)
                .build();

        mockMvc.perform(put("/api/v1/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Nhân viên chỉ có quyền chỉnh sửa công việc của chính mình hoặc được phân công"));
    }

    @Test
    @DisplayName("EMPLOYEE cố tình hoàn thành công việc của nhân viên khác bị chặn 403 Forbidden")
    @WithMockUser(username = "dev_hoa", roles = {"EMPLOYEE"})
    void employeeCompletingOtherEmployeeTask_Returns403() throws Exception {
        // Task 1 do admin tạo và giao cho dev_nam, dev_hoa không có quyền nộp hoàn thành
        TaskCompleteRequest request = TaskCompleteRequest.builder()
                .completionNote("Hoa hoàn thành hộ Nam")
                .proofUrl("https://github.com/techcorp/ecommerce/pull/99")
                .build();

        mockMvc.perform(patch("/api/v1/tasks/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Nhân viên chỉ có quyền hoàn thành công việc của chính mình hoặc được phân công"));
    }

    @Test
    @DisplayName("Đăng ký tài khoản công khai với role ADMIN bị chặn 400 Bad Request (chống leo quyền)")
    void publicRegister_WithAdminRole_Returns400BadRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("attacker_admin")
                .password("admin123456")
                .fullName("Attacker")
                .email("attacker@evil.com")
                .role(UserRole.ADMIN)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Đăng ký tài khoản công khai chỉ áp dụng cho vai trò Khách hàng (CLIENT)"));
    }

    @Test
    @DisplayName("Khách hàng tạo nhanh task (NLP) không truyền projectId tự động gán vào dự án của khách hàng")
    @WithMockUser(username = "client_fpt", roles = {"CLIENT"})
    void clientQuickTask_WithoutProjectId_DefaultsToClientProject() throws Exception {
        // client_fpt sở hữu Dự án 2 (devops)
        QuickTaskRequest request = QuickTaskRequest.builder()
                .text("Họp nghiệm thu CI CD lúc 15:00 ngày mai mức độ HIGH")
                .build();

        mockMvc.perform(post("/api/v1/tasks/quick-parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectName").value("Nâng cấp Hạ tầng & CI/CD"));
    }

    @Test
    @DisplayName("Không thể thay đổi subtask của công việc đã hoàn thành (DONE) trả về 400 Bad Request")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void toggleSubtask_OnDoneTask_Returns400BadRequest() throws Exception {
        // Đảm bảo Task 1 ở trạng thái DONE
        TaskCompleteRequest completeReq = TaskCompleteRequest.builder()
                .completionNote("Nghiệm thu hoàn tất đầy đủ")
                .build();
        mockMvc.perform(patch("/api/v1/tasks/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isOk());

        // Khi Task 1 đã DONE, cố tình toggle subtask phải bị chặn 400
        mockMvc.perform(patch("/api/v1/subtasks/1/toggle"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Không thể thay đổi trạng thái công việc con khi công việc chính đã hoàn thành (DONE). Vui lòng chuyển trạng thái công việc sang Đang làm hoặc Cần sửa lại trước."));
    }
}

