package com.portfolio.taskmanager.service;

import com.portfolio.taskmanager.dto.request.QuickTaskRequest;
import com.portfolio.taskmanager.dto.request.TaskCreateRequest;
import com.portfolio.taskmanager.dto.request.TaskUpdateRequest;
import com.portfolio.taskmanager.dto.response.PageResponse;
import com.portfolio.taskmanager.dto.response.TaskResponse;
import com.portfolio.taskmanager.dto.response.TaskScoreResponse;
import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {

    TaskResponse createTask(TaskCreateRequest request);

    PageResponse<TaskResponse> getTasks(
            TaskStatus status,
            TaskPriority priority,
            Long projectId,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            String keyword,
            Pageable pageable
    );

    PageResponse<TaskResponse> getTasks(
            TaskStatus status,
            TaskPriority priority,
            Long projectId,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            String keyword,
            Long assigneeId,
            Pageable pageable
    );

    PageResponse<TaskResponse> getTasks(
            TaskStatus status,
            TaskPriority priority,
            Long projectId,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            String keyword,
            Long assigneeId,
            String timeRange,
            String timeField,
            Pageable pageable
    );

    PageResponse<TaskResponse> getMyTasks(Pageable pageable);

    TaskResponse assignTask(Long taskId, Long userId);

    TaskResponse getTaskById(Long id);

    TaskResponse updateTask(Long id, TaskUpdateRequest request);

    void deleteTask(Long id);

    TaskResponse markTaskComplete(Long id, com.portfolio.taskmanager.dto.request.TaskCompleteRequest request);

    TaskResponse markTaskComplete(Long id);

    TaskResponse updateTaskStatus(Long id, TaskStatus status);

    TaskResponse updateTaskStatus(Long id, TaskStatus status, String reworkNote);

    List<TaskScoreResponse> getSmartPriorityTasks();

    TaskResponse createQuickTask(QuickTaskRequest request);
}

