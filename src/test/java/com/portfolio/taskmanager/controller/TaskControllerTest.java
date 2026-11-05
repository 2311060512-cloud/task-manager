package com.portfolio.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.taskmanager.dto.request.QuickTaskRequest;
import com.portfolio.taskmanager.dto.request.TaskCompleteRequest;
import com.portfolio.taskmanager.dto.request.TaskCreateRequest;
import com.portfolio.taskmanager.dto.response.PageResponse;
import com.portfolio.taskmanager.dto.response.TaskResponse;
import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.exception.BadRequestException;
import com.portfolio.taskmanager.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private com.portfolio.taskmanager.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.portfolio.taskmanager.security.CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/v1/tasks - Tạo công việc hợp lệ trả về 201 Created")
    void createTask_Valid_Returns201() throws Exception {
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("Viết API phân quyền JWT")
                .description("Phân quyền theo Role ADMIN/USER")
                .dueDate(LocalDateTime.now().plusDays(2))
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .projectId(1L)
                .build();

        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Viết API phân quyền JWT")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .projectId(1L)
                .projectName("Dự án Demo")
                .subtasks(Collections.emptyList())
                .progress(0.0)
                .overdue(false)
                .build();

        when(taskService.createTask(any(TaskCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("Viết API phân quyền JWT"));
    }

    @Test
    @DisplayName("POST /api/v1/tasks - Tiêu đề rỗng trả về 400 Bad Request")
    void createTask_Invalid_Returns400() throws Exception {
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("") // Blank title
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    @DisplayName("PATCH /api/v1/tasks/{id}/status - Cập nhật trạng thái trả về 200 OK")
    void updateTaskStatus_Returns200() throws Exception {
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Task 1")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(taskService.updateTaskStatus(1L, TaskStatus.IN_PROGRESS)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tasks/1/status")
                        .param("status", "in_progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("GET /api/v1/tasks - Lấy danh sách công việc phân trang trả về 200 OK")
    void getTasks_Returns200() throws Exception {
        TaskResponse t = TaskResponse.builder()
                .id(1L)
                .title("Task 1")
                .build();

        PageResponse<TaskResponse> page = PageResponse.<TaskResponse>builder()
                .content(List.of(t))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        when(taskService.getTasks(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1L));
    }

    @Test
    @DisplayName("PATCH /api/v1/tasks/{id}/complete - Đánh dấu hoàn thành kèm minh chứng trả về 200 OK")
    void markTaskComplete_Returns200() throws Exception {
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Task 1")
                .status(TaskStatus.DONE)
                .progress(100.0)
                .completionNote("Đã deploy và kiểm thử thành công")
                .proofUrl("https://github.com/techcorp/ecommerce/pull/12")
                .build();

        TaskCompleteRequest completeRequest = TaskCompleteRequest.builder()
                .completionNote("Đã deploy và kiểm thử thành công")
                .proofUrl("https://github.com/techcorp/ecommerce/pull/12")
                .build();

        when(taskService.markTaskComplete(eq(1L), any(TaskCompleteRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tasks/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DONE"))
                .andExpect(jsonPath("$.data.progress").value(100.0));
    }

    @Test
    @DisplayName("PATCH /api/v1/tasks/{id}/complete - Thiếu minh chứng hoàn thành trả về 400 Bad Request")
    void markTaskComplete_WithoutProof_Returns400() throws Exception {
        when(taskService.markTaskComplete(eq(1L), any()))
                .thenThrow(new BadRequestException("Bắt buộc phải cung cấp minh chứng hoàn thành công việc"));

        mockMvc.perform(patch("/api/v1/tasks/1/complete"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bắt buộc phải cung cấp minh chứng hoàn thành công việc"));
    }

    @Test
    @DisplayName("POST /api/v1/tasks/quick-parse - Phân tích câu tự nhiên tạo Task trả về 201 Created")
    void createQuickTask_Returns201() throws Exception {
        QuickTaskRequest request = QuickTaskRequest.builder()
                .text("Họp khách hàng lúc 14:00 ngày mai")
                .build();

        TaskResponse response = TaskResponse.builder()
                .id(5L)
                .title("Họp khách hàng")
                .status(TaskStatus.TODO)
                .build();

        when(taskService.createQuickTask(any(QuickTaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks/quick-parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Họp khách hàng"));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/my-tasks - Lấy danh sách việc của tôi trả về 200 OK")
    void getMyTasks_Returns200() throws Exception {
        TaskResponse t = TaskResponse.builder().id(10L).title("Việc của tôi").build();
        PageResponse<TaskResponse> page = PageResponse.<TaskResponse>builder()
                .content(List.of(t))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        when(taskService.getMyTasks(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/tasks/my-tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(10L));
    }

    @Test
    @DisplayName("PATCH /api/v1/tasks/{id}/assign - Giao việc trả về 200 OK")
    void assignTask_Returns200() throws Exception {
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Task 1")
                .assigneeId(2L)
                .assigneeName("Nam Dev")
                .build();

        when(taskService.assignTask(eq(1L), eq(2L))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tasks/1/assign").param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assigneeId").value(2L));
    }
}

