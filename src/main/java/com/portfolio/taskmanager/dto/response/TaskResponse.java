package com.portfolio.taskmanager.dto.response;

import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private TaskPriority priority;
    private String priorityDisplayName;
    private TaskStatus status;
    private String statusDisplayName;
    private Long projectId;
    private String projectName;
    private Long assigneeId;
    private String assigneeName;
    private List<SubtaskResponse> subtasks;
    private double progress;
    private boolean overdue;
    private String reworkNote;
    private String completionNote;
    private String proofUrl;
    private LocalDateTime completedAt;
    private Long completedById;
    private String completedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
