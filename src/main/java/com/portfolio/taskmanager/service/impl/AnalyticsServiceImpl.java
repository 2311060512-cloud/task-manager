package com.portfolio.taskmanager.service.impl;

import com.portfolio.taskmanager.dto.response.DashboardStatisticsResponse;
import com.portfolio.taskmanager.entity.Project;
import com.portfolio.taskmanager.entity.Task;
import com.portfolio.taskmanager.entity.User;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.entity.enums.UserRole;
import com.portfolio.taskmanager.repository.ProjectRepository;
import com.portfolio.taskmanager.repository.TaskRepository;
import com.portfolio.taskmanager.security.SecurityUtils;
import com.portfolio.taskmanager.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SecurityUtils securityUtils;

    @Override
    public DashboardStatisticsResponse getDashboardStatistics() {
        Optional<User> currentUserOpt = securityUtils.getCurrentUser();
        boolean isClient = currentUserOpt.isPresent() && currentUserOpt.get().getRole() == UserRole.CLIENT;

        List<Project> projects = isClient
                ? projectRepository.findByClientId(currentUserOpt.get().getId())
                : projectRepository.findAll();

        long totalProjects = projects.size();

        List<Task> allAccessibleTasks = new ArrayList<>();
        for (Project project : projects) {
            if (project.getTasks() != null) {
                allAccessibleTasks.addAll(project.getTasks());
            }
        }

        long totalTasks = allAccessibleTasks.size();

        Map<TaskStatus, Long> tasksByStatus = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            long count = allAccessibleTasks.stream().filter(t -> t.getStatus() == status).count();
            tasksByStatus.put(status, count);
        }

        LocalDateTime now = LocalDateTime.now();
        long overdueTasks = allAccessibleTasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(now) && t.getStatus() != TaskStatus.DONE)
                .count();

        long doneTasks = tasksByStatus.getOrDefault(TaskStatus.DONE, 0L);
        double overallCompletionRate = totalTasks > 0
                ? Math.round(((double) doneTasks / totalTasks * 100.0) * 10.0) / 10.0
                : 0.0;

        List<DashboardStatisticsResponse.ProjectSummary> projectSummaries = new ArrayList<>();
        for (Project project : projects) {
            List<Task> tasks = project.getTasks() != null ? project.getTasks() : List.of();
            long projTotal = tasks.size();
            long projDone = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
            double projRate = projTotal > 0
                    ? Math.round(((double) projDone / projTotal * 100.0) * 10.0) / 10.0
                    : 0.0;

            projectSummaries.add(DashboardStatisticsResponse.ProjectSummary.builder()
                    .projectId(project.getId())
                    .projectName(project.getName())
                    .totalTasks(projTotal)
                    .completedTasks(projDone)
                    .completionRate(projRate)
                    .build());
        }

        return DashboardStatisticsResponse.builder()
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .tasksByStatus(tasksByStatus)
                .overdueTasks(overdueTasks)
                .overallCompletionRate(overallCompletionRate)
                .projectSummaries(projectSummaries)
                .build();
    }
}

