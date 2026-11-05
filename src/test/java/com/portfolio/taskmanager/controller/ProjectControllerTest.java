package com.portfolio.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.taskmanager.dto.request.ProjectCreateRequest;
import com.portfolio.taskmanager.dto.response.ProjectDetailResponse;
import com.portfolio.taskmanager.dto.response.ProjectResponse;
import com.portfolio.taskmanager.security.CustomUserDetailsService;
import com.portfolio.taskmanager.security.JwtTokenProvider;
import com.portfolio.taskmanager.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/v1/projects - Tạo dự án thành công trả về 201 Created")
    void createProject_Valid_Returns201() throws Exception {
        ProjectCreateRequest request = ProjectCreateRequest.builder()
                .name("Hệ thống ERP")
                .description("Phần mềm quản lý tài nguyên doanh nghiệp")
                .build();

        ProjectResponse response = ProjectResponse.builder()
                .id(1L)
                .name("Hệ thống ERP")
                .description("Phần mềm quản lý tài nguyên doanh nghiệp")
                .totalTasks(0)
                .completedTasks(0)
                .completionRate(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(projectService.createProject(any(ProjectCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Hệ thống ERP"));
    }

    @Test
    @DisplayName("POST /api/v1/projects - Tên rỗng trả về 400 Bad Request kèm validation error")
    void createProject_BlankName_Returns400() throws Exception {
        ProjectCreateRequest request = ProjectCreateRequest.builder()
                .name("") // Invalid blank name
                .description("Mô tả")
                .build();

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    @DisplayName("GET /api/v1/projects - Lấy danh sách dự án trả về 200 OK")
    void getAllProjects_Returns200() throws Exception {
        ProjectResponse p = ProjectResponse.builder()
                .id(1L)
                .name("Dự án Alpha")
                .build();

        when(projectService.getAllProjects()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Dự án Alpha"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id} - Lấy chi tiết dự án trả về 200 OK")
    void getProjectById_Returns200() throws Exception {
        ProjectDetailResponse detail = ProjectDetailResponse.builder()
                .id(1L)
                .name("Dự án Alpha")
                .tasks(new ArrayList<>())
                .build();

        when(projectService.getProjectById(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/v1/projects/{id} - Xóa dự án trả về 204 No Content")
    void deleteProject_Returns204() throws Exception {
        doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/v1/projects/1"))
                .andExpect(status().isNoContent());
    }
}
