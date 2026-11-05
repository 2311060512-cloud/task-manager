package com.portfolio.taskmanager.entity;

import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(name = "completion_note", length = 2000)
    private String completionNote;

    @Column(name = "proof_url", columnDefinition = "TEXT")
    private String proofUrl;

    @Column(name = "rework_note", length = 2000)
    private String reworkNote;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by_id")
    private User completedBy;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Subtask> subtasks = new ArrayList<>();

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
        subtask.setTask(this);
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
        subtask.setTask(null);
    }

    /**
     * Tự động tính tiến độ công việc (%)
     * - Nếu có subtask: tỷ lệ subtask đã hoàn thành.
     * - Nếu không có subtask: 100% nếu DONE, 50% nếu IN_PROGRESS, 0% nếu TODO.
     */
    public double calculateProgress() {
        if (subtasks != null && !subtasks.isEmpty()) {
            long completedCount = subtasks.stream().filter(Subtask::isCompleted).count();
            return Math.round(((double) completedCount / subtasks.size() * 100.0) * 10.0) / 10.0;
        }
        return switch (status) {
            case DONE -> 100.0;
            case IN_PROGRESS -> 50.0;
            case REWORK -> 50.0;
            case TODO -> 0.0;
        };
    }

    /**
     * Đánh dấu công việc hoàn thành kèm minh chứng
     */
    public void markAsCompleted(String note, String proof, User user) {
        this.status = TaskStatus.DONE;
        this.completionNote = note;
        this.proofUrl = proof;
        this.completedAt = LocalDateTime.now();
        this.completedBy = user;
    }

    /**
     * Đánh dấu công việc hoàn thành
     */
    public void markAsCompleted() {
        this.status = TaskStatus.DONE;
        if (this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }
}

