package com.portfolio.taskmanager.service.smart;

import com.portfolio.taskmanager.dto.response.TaskScoreResponse;
import com.portfolio.taskmanager.entity.Task;
import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.mapper.SubtaskMapper;
import com.portfolio.taskmanager.mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TaskPriorityCalculatorTest {

    private TaskPriorityCalculator priorityCalculator;

    @BeforeEach
    void setUp() {
        SubtaskMapper subtaskMapper = new SubtaskMapper();
        TaskMapper taskMapper = new TaskMapper(subtaskMapper);
        priorityCalculator = new TaskPriorityCalculator(taskMapper);
    }

    @Test
    @DisplayName("Task đã hoàn thành (DONE) phải có điểm ưu tiên bằng 0")
    void shouldReturnZeroScore_WhenTaskIsDone() {
        Task task = Task.builder()
                .id(1L)
                .title("Đã hoàn thành")
                .priority(TaskPriority.URGENT)
                .status(TaskStatus.DONE)
                .dueDate(LocalDateTime.now().minusDays(2))
                .build();

        TaskScoreResponse score = priorityCalculator.calculateScore(task);

        assertThat(score.getTotalScore()).isZero();
        assertThat(score.getPriorityScore()).isZero();
        assertThat(score.getUrgencyScore()).isZero();
        assertThat(score.getScoreExplanation()).contains("DONE");
    }

    @Test
    @DisplayName("Task quá hạn phải nhận điểm phạt khẩn cấp cao nhất (>= 100)")
    void shouldReturnHighUrgency_WhenTaskIsOverdue() {
        Task task = Task.builder()
                .id(2L)
                .title("Fix bug thanh toán")
                .priority(TaskPriority.HIGH) // 50
                .status(TaskStatus.IN_PROGRESS) // +15
                .dueDate(LocalDateTime.now().minusDays(3)) // overdue 3 days -> 100 + 15 = 115
                .build();

        TaskScoreResponse score = priorityCalculator.calculateScore(task);

        assertThat(score.getPriorityScore()).isEqualTo(50);
        assertThat(score.getUrgencyScore()).isGreaterThanOrEqualTo(100);
        assertThat(score.getStatusModifier()).isEqualTo(15);
        assertThat(score.getTotalScore()).isEqualTo(50 + score.getUrgencyScore() + 15);
    }

    @Test
    @DisplayName("Task đến hạn hôm nay phải có điểm khẩn cấp là 90")
    void shouldReturn90Urgency_WhenTaskIsDueToday() {
        Task task = Task.builder()
                .id(3L)
                .title("Gửi báo cáo tuần")
                .priority(TaskPriority.URGENT) // 80
                .status(TaskStatus.TODO) // 0
                .dueDate(LocalDateTime.now().withHour(17).withMinute(0))
                .build();

        TaskScoreResponse score = priorityCalculator.calculateScore(task);

        assertThat(score.getPriorityScore()).isEqualTo(80);
        assertThat(score.getUrgencyScore()).isEqualTo(90);
        assertThat(score.getStatusModifier()).isZero();
        assertThat(score.getTotalScore()).isEqualTo(170);
    }

    @Test
    @DisplayName("Task không có hạn chót nhận điểm khẩn cấp mặc định là 5")
    void shouldReturnDefaultUrgency_WhenTaskHasNoDueDate() {
        Task task = Task.builder()
                .id(4L)
                .title("Tài liệu hướng dẫn")
                .priority(TaskPriority.LOW) // 10
                .status(TaskStatus.TODO) // 0
                .dueDate(null)
                .build();

        TaskScoreResponse score = priorityCalculator.calculateScore(task);

        assertThat(score.getPriorityScore()).isEqualTo(10);
        assertThat(score.getUrgencyScore()).isEqualTo(5);
        assertThat(score.getStatusModifier()).isZero();
        assertThat(score.getTotalScore()).isEqualTo(15);
    }

    @Test
    @DisplayName("Task cần sửa lại (REWORK) phải nhận thêm 25 điểm ưu tiên khẩn cấp")
    void shouldReturn25StatusModifier_WhenTaskIsRework() {
        Task task = Task.builder()
                .id(5L)
                .title("Lỗi giao diện checkout cần sửa gấp")
                .priority(TaskPriority.HIGH) // 50
                .status(TaskStatus.REWORK) // +25
                .dueDate(LocalDateTime.now().plusDays(2)) // +50
                .build();

        TaskScoreResponse score = priorityCalculator.calculateScore(task);

        assertThat(score.getStatusModifier()).isEqualTo(25);
        assertThat(score.getScoreExplanation()).contains("REWORK");
        assertThat(score.getTotalScore()).isEqualTo(50 + score.getUrgencyScore() + 25);
    }
}

