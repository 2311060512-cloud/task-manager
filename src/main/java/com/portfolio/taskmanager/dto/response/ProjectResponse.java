package com.portfolio.taskmanager.dto.response;

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
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private Long clientId;
    private String clientName;
    private List<String> memberNames;
    private long totalTasks;
    private long completedTasks;
    private double completionRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
