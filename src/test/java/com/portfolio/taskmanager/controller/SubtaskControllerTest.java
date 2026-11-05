package com.portfolio.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.taskmanager.dto.request.SubtaskCreateRequest;
import com.portfolio.taskmanager.dto.request.SubtaskUpdateRequest;
import com.portfolio.taskmanager.dto.response.SubtaskResponse;
import com.portfolio.taskmanager.security.CustomUserDetailsService;
import com.portfolio.taskmanager.security.JwtTokenProvider;
import com.portfolio.taskmanager.service.SubtaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubtaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubtaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubtaskService subtaskService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/v1/tasks/{taskId}/subtasks - Tạo subtask trả về 201 Created")
    void createSubtask_Success() throws Exception {
        SubtaskCreateRequest request = SubtaskCreateRequest.builder()
                .title("Tạo bảng CSDL")
                .build();

        SubtaskResponse response = SubtaskResponse.builder()
                .id(1L)
                .title("Tạo bảng CSDL")
                .completed(false)
                .build();

        when(subtaskService.createSubtask(eq(1L), any(SubtaskCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks/1/subtasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Tạo bảng CSDL"));
    }

    @Test
    @DisplayName("PATCH /api/v1/subtasks/{id}/toggle - Đổi trạng thái subtask trả về 200 OK")
    void toggleSubtask_Success() throws Exception {
        SubtaskResponse response = SubtaskResponse.builder()
                .id(1L)
                .title("Tạo bảng CSDL")
                .completed(true)
                .build();

        when(subtaskService.toggleSubtask(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/subtasks/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.completed").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/subtasks/{id} - Sửa subtask trả về 200 OK")
    void updateSubtask_Success() throws Exception {
        SubtaskUpdateRequest request = SubtaskUpdateRequest.builder()
                .title("Đổi tên subtask")
                .build();

        SubtaskResponse response = SubtaskResponse.builder()
                .id(1L)
                .title("Đổi tên subtask")
                .completed(false)
                .build();

        when(subtaskService.updateSubtask(eq(1L), any(SubtaskUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/subtasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Đổi tên subtask"));
    }

    @Test
    @DisplayName("DELETE /api/v1/subtasks/{id} - Xóa subtask trả về 204 No Content")
    void deleteSubtask_Success() throws Exception {
        doNothing().when(subtaskService).deleteSubtask(1L);

        mockMvc.perform(delete("/api/v1/subtasks/1"))
                .andExpect(status().isNoContent());
    }
}

