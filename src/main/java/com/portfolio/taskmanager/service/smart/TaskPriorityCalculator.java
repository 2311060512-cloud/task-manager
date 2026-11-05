package com.portfolio.taskmanager.service.smart;

import com.portfolio.taskmanager.dto.response.TaskResponse;
import com.portfolio.taskmanager.dto.response.TaskScoreResponse;
import com.portfolio.taskmanager.entity.Task;
import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Thuật toán tính điểm ưu tiên thông minh cho công việc.
 * Công thức: Total Score = PriorityWeight + UrgencyScore + StatusModifier
 * - Task DONE: 0 điểm
 * - Quá hạn (Overdue): UrgencyScore phạt cao nhất (100 - 150 điểm)
 * - Đến hạn hôm nay: 90 điểm
 * - Sắp đến hạn (1-3 ngày): 40 - 60 điểm
 * - Đang thực hiện (IN_PROGRESS): +15 điểm ưu tiên chốt dở dang
 */
@Component
@RequiredArgsConstructor
public class TaskPriorityCalculator {

    private final TaskMapper taskMapper;

    public TaskScoreResponse calculateScore(Task task) {
        TaskResponse taskResponse = taskMapper.toResponse(task);

        if (task.getStatus() == TaskStatus.DONE) {
            return TaskScoreResponse.builder()
                    .task(taskResponse)
                    .priorityScore(0)
                    .urgencyScore(0)
                    .statusModifier(0)
                    .totalScore(0)
                    .scoreExplanation("Công việc đã hoàn thành (DONE) - Không cần ưu tiên thêm")
                    .build();
        }

        // 1. Trọng số mức ưu tiên
        int priorityWeight = task.getPriority() != null ? task.getPriority().getWeight() : TaskPriority.MEDIUM.getWeight();

        // 2. Điểm khẩn cấp theo hạn chót (Urgency Score)
        int urgencyScore = 5;
        String urgencyReason = "Không có hạn chót cụ thể (+5 điểm)";

        if (task.getDueDate() != null) {
            LocalDate dueDate = task.getDueDate().toLocalDate();
            LocalDate today = LocalDate.now();
            long daysRemaining = ChronoUnit.DAYS.between(today, dueDate);

            if (daysRemaining < 0) {
                // Quá hạn: phạt khẩn cấp 100 điểm + 5 điểm cho mỗi ngày trễ (tối đa +50)
                int overduePenalty = (int) Math.min(Math.abs(daysRemaining) * 5, 50);
                urgencyScore = 100 + overduePenalty;
                urgencyReason = String.format("Quá hạn %d ngày (Khẩn cấp cao nhất: +%d điểm)", Math.abs(daysRemaining), urgencyScore);
            } else if (daysRemaining == 0) {
                urgencyScore = 90;
                urgencyReason = "Hạn chót là hôm nay (+90 điểm)";
            } else if (daysRemaining <= 3) {
                urgencyScore = 70 - ((int) daysRemaining * 10);
                urgencyReason = String.format("Đến hạn trong %d ngày tới (+%d điểm)", daysRemaining, urgencyScore);
            } else if (daysRemaining <= 7) {
                urgencyScore = 35;
                urgencyReason = String.format("Đến hạn trong tuần tới (%d ngày, +35 điểm)", daysRemaining);
            } else {
                urgencyScore = 10;
                urgencyReason = String.format("Còn dài hạn (%d ngày, +10 điểm)", daysRemaining);
            }
        }

        // 3. Hệ số bổ sung theo trạng thái (Status Modifier)
        int statusModifier = 0;
        String statusReason = "Chưa bắt đầu (TODO: 0 điểm)";
        if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            statusModifier = 15;
            statusReason = "Đang thực hiện dở dang (IN_PROGRESS: +15 điểm)";
        } else if (task.getStatus() == TaskStatus.REWORK) {
            statusModifier = 25;
            statusReason = "Cần sửa lại theo phản hồi lỗi (REWORK: +25 điểm khẩn cấp)";
        }

        int totalScore = priorityWeight + urgencyScore + statusModifier;
        String explanation = String.format(
                "Ưu tiên %s (+%d) | %s | %s => Tổng: %d điểm",
                task.getPriority(), priorityWeight, urgencyReason, statusReason, totalScore
        );

        return TaskScoreResponse.builder()
                .task(taskResponse)
                .priorityScore(priorityWeight)
                .urgencyScore(urgencyScore)
                .statusModifier(statusModifier)
                .totalScore(totalScore)
                .scoreExplanation(explanation)
                .build();
    }
}

