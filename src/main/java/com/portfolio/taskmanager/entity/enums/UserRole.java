package com.portfolio.taskmanager.entity.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("Quản trị viên"),
    EMPLOYEE("Nhân viên"),
    CLIENT("Khách hàng");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }
}

