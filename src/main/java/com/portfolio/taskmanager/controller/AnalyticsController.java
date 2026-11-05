package com.portfolio.taskmanager.controller;

import com.portfolio.taskmanager.dto.response.ApiResponse;
import com.portfolio.taskmanager.dto.response.DashboardStatisticsResponse;
import com.portfolio.taskmanager.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "APIs Thống kê & Báo cáo tiến độ")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Lấy dữ liệu thống kê tổng quan",
            description = "Trả về số lượng công việc theo trạng thái, công việc quá hạn, tỷ lệ hoàn thành dự án và hệ thống")
    public ApiResponse<DashboardStatisticsResponse> getDashboardStatistics() {
        DashboardStatisticsResponse response = analyticsService.getDashboardStatistics();
        return ApiResponse.success("Lấy dữ liệu thống kê thành công", response);
    }
}

