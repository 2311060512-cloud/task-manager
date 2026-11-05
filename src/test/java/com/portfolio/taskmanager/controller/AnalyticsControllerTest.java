package com.portfolio.taskmanager.controller;

import com.portfolio.taskmanager.dto.response.DashboardStatisticsResponse;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.security.CustomUserDetailsService;
import com.portfolio.taskmanager.security.JwtTokenProvider;
import com.portfolio.taskmanager.service.AnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("GET /api/v1/analytics/dashboard - Trả về 200 OK cùng số liệu thống kê")
    void getDashboardStatistics_Success() throws Exception {
        DashboardStatisticsResponse mockResponse = DashboardStatisticsResponse.builder()
                .totalProjects(3)
                .totalTasks(12)
                .overdueTasks(2)
                .overallCompletionRate(66.7)
                .tasksByStatus(Map.of(TaskStatus.TODO, 2L, TaskStatus.IN_PROGRESS, 2L, TaskStatus.DONE, 8L))
                .projectSummaries(List.of(
                        DashboardStatisticsResponse.ProjectSummary.builder()
                                .projectId(1L)
                                .projectName("Hệ thống ERP")
                                .totalTasks(5)
                                .completedTasks(4)
                                .completionRate(80.0)
                                .build()
                ))
                .build();

        when(analyticsService.getDashboardStatistics()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/analytics/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalProjects").value(3))
                .andExpect(jsonPath("$.data.overallCompletionRate").value(66.7));
    }
}

