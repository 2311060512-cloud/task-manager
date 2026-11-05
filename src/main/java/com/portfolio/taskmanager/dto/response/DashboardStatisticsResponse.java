package com.portfolio.taskmanager.dto.response;

import com.portfolio.taskmanager.entity.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatisticsResponse {

    private long totalProjects;
    private long totalTasks;
    private Map<TaskStatus, Long> tasksByStatus;
    private long overdueTasks;
    private double overallCompletionRate;
    private List<ProjectSummary> projectSummaries;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectSummary {
        private Long projectId;
        private String projectName;
        private long totalTasks;
        private long completedTasks;
        private double completionRate;
    }
}

