package com.portfolio.taskmanager.mapper;

import com.portfolio.taskmanager.dto.response.TaskResponse;
import com.portfolio.taskmanager.entity.Task;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final SubtaskMapper subtaskMapper;

    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }

        boolean isOverdue = task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDateTime.now())
                && task.getStatus() != TaskStatus.DONE;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .priorityDisplayName(task.getPriority() != null ? task.getPriority().getDisplayName() : null)
                .status(task.getStatus())
                .statusDisplayName(task.getStatus() != null ? task.getStatus().getDisplayName() : null)
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .projectName(task.getProject() != null ? task.getProject().getName() : null)
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getFullName() : null)
                .subtasks(subtaskMapper.toResponseList(task.getSubtasks()))
                .progress(task.calculateProgress())
                .overdue(isOverdue)
                .reworkNote(task.getReworkNote())
                .completionNote(task.getCompletionNote())
                .proofUrl(task.getProofUrl())
                .completedAt(task.getCompletedAt())
                .completedById(task.getCompletedBy() != null ? task.getCompletedBy().getId() : null)
                .completedByName(task.getCompletedBy() != null ? task.getCompletedBy().getFullName() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public List<TaskResponse> toResponseList(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }
        return tasks.stream()
                .map(this::toResponse)
                .toList();
    }
}

