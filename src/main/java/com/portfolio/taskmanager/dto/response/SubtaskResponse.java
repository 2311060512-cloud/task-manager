package com.portfolio.taskmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubtaskResponse {

    private Long id;
    private String title;
    private boolean completed;
    private Long taskId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

