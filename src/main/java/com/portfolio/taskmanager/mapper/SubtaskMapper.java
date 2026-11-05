package com.portfolio.taskmanager.mapper;

import com.portfolio.taskmanager.dto.response.SubtaskResponse;
import com.portfolio.taskmanager.entity.Subtask;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SubtaskMapper {

    public SubtaskResponse toResponse(Subtask subtask) {
        if (subtask == null) {
            return null;
        }

        return SubtaskResponse.builder()
                .id(subtask.getId())
                .title(subtask.getTitle())
                .completed(subtask.isCompleted())
                .taskId(subtask.getTask() != null ? subtask.getTask().getId() : null)
                .createdAt(subtask.getCreatedAt())
                .updatedAt(subtask.getUpdatedAt())
                .build();
    }

    public List<SubtaskResponse> toResponseList(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            return Collections.emptyList();
        }
        return subtasks.stream()
                .map(this::toResponse)
                .toList();
    }
}

