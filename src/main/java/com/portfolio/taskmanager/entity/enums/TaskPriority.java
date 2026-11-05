package com.portfolio.taskmanager.entity.enums;

import lombok.Getter;

@Getter
public enum TaskPriority {
    LOW(10, "Thấp"),
    MEDIUM(25, "Trung bình"),
    HIGH(50, "Cao"),
    URGENT(80, "Khẩn cấp");

    private final int weight;
    private final String displayName;

    TaskPriority(int weight, String displayName) {
        this.weight = weight;
        this.displayName = displayName;
    }
}
