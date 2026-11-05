package com.portfolio.taskmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskScoreResponse {

    private TaskResponse task;
    private int priorityScore;
    private int urgencyScore;
    private int statusModifier;
    private int totalScore;
    private String scoreExplanation;
}

