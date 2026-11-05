package com.portfolio.taskmanager.mapper;

import com.portfolio.taskmanager.dto.response.ProjectDetailResponse;
import com.portfolio.taskmanager.dto.response.ProjectResponse;
import com.portfolio.taskmanager.entity.Project;
import com.portfolio.taskmanager.entity.Task;
import com.portfolio.taskmanager.entity.User;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectMapper {

    private final TaskMapper taskMapper;

    public ProjectResponse toResponse(Project project) {
        if (project == null) {
            return null;
        }

        List<Task> tasks = project.getTasks() != null ? project.getTasks() : Collections.emptyList();
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        double rate = totalTasks > 0 ? Math.round(((double) completedTasks / totalTasks * 100.0) * 10.0) / 10.0 : 0.0;

        List<String> memberNames = project.getMembers() != null
                ? project.getMembers().stream().map(User::getFullName).toList()
                : Collections.emptyList();

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .clientId(project.getClient() != null ? project.getClient().getId() : null)
                .clientName(project.getClient() != null ? project.getClient().getFullName() : null)
                .memberNames(memberNames)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .completionRate(rate)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    public ProjectDetailResponse toDetailResponse(Project project) {
        if (project == null) {
            return null;
        }

        List<Task> tasks = project.getTasks() != null ? project.getTasks() : Collections.emptyList();
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        double rate = totalTasks > 0 ? Math.round(((double) completedTasks / totalTasks * 100.0) * 10.0) / 10.0 : 0.0;

        List<String> memberNames = project.getMembers() != null
                ? project.getMembers().stream().map(User::getFullName).toList()
                : Collections.emptyList();

        return ProjectDetailResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .clientId(project.getClient() != null ? project.getClient().getId() : null)
                .clientName(project.getClient() != null ? project.getClient().getFullName() : null)
                .memberNames(memberNames)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .completionRate(rate)
                .tasks(taskMapper.toResponseList(tasks))
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    public List<ProjectResponse> toResponseList(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            return Collections.emptyList();
        }
        return projects.stream()
                .map(this::toResponse)
                .toList();
    }
}
