package com.portfolio.taskmanager.entity.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {
    TODO("Cần làm"),
    IN_PROGRESS("Đang thực hiện"),
    REWORK("Cần sửa lại"),
    DONE("Đã hoàn thành");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }
}
