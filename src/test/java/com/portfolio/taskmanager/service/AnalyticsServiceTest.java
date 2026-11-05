package com.portfolio.taskmanager.service;

import com.portfolio.taskmanager.dto.response.DashboardStatisticsResponse;
import com.portfolio.taskmanager.entity.Project;
import com.portfolio.taskmanager.entity.Task;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.repository.ProjectRepository;
import com.portfolio.taskmanager.repository.TaskRepository;
import com.portfolio.taskmanager.security.SecurityUtils;
import com.portfolio.taskmanager.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    @DisplayName("getDashboardStatistics - Tính toán số liệu thống kê chính xác")
    void getDashboardStatistics_CalculatesAccurateMetrics() {
        Task taskDone1 = Task.builder().id(1L).status(TaskStatus.DONE).dueDate(LocalDateTime.now().plusDays(2)).build();
        Task taskDone2 = Task.builder().id(2L).status(TaskStatus.DONE).dueDate(LocalDateTime.now().plusDays(3)).build();
        Task taskTodo = Task.builder().id(3L).status(TaskStatus.TODO).dueDate(LocalDateTime.now().minusDays(1)).build();
        Task taskInProgress = Task.builder().id(4L).status(TaskStatus.IN_PROGRESS).dueDate(LocalDateTime.now().plusDays(1)).build();

        Project proj1 = Project.builder()
                .id(1L)
                .name("Dự án Alpha")
                .tasks(List.of(taskDone1, taskTodo))
                .build();

        Project proj2 = Project.builder()
                .id(2L)
                .name("Dự án Beta")
                .tasks(List.of(taskDone2, taskInProgress))
                .build();

        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());
        when(projectRepository.findAll()).thenReturn(List.of(proj1, proj2));

        DashboardStatisticsResponse stats = analyticsService.getDashboardStatistics();

        assertThat(stats).isNotNull();
        assertThat(stats.getTotalProjects()).isEqualTo(2L);
        assertThat(stats.getTotalTasks()).isEqualTo(4L);
        assertThat(stats.getOverdueTasks()).isEqualTo(1L); // taskTodo is overdue
        assertThat(stats.getOverallCompletionRate()).isEqualTo(50.0); // 2 out of 4 = 50.0%
        assertThat(stats.getTasksByStatus().get(TaskStatus.DONE)).isEqualTo(2L);
        assertThat(stats.getProjectSummaries()).hasSize(2);
        assertThat(stats.getProjectSummaries().get(0).getCompletionRate()).isEqualTo(50.0); // 1 out of 2 = 50.0%
        assertThat(stats.getProjectSummaries().get(1).getCompletionRate()).isEqualTo(50.0); // 1 out of 2 = 50.0%
    }
}
