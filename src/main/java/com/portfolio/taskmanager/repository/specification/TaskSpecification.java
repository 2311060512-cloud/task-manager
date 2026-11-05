package com.portfolio.taskmanager.repository.specification;

import com.portfolio.taskmanager.entity.Task;
import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    private TaskSpecification() {
    }

    public static Specification<Task> filter(
            TaskStatus status,
            TaskPriority priority,
            Long projectId,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            String keyword
    ) {
        return filter(status, priority, projectId, dueDateFrom, dueDateTo, keyword, null, null, null, null);
    }

    public static Specification<Task> filter(
            TaskStatus status,
            TaskPriority priority,
            Long projectId,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            String keyword,
            Long assigneeId,
            List<Long> allowedProjectIds
    ) {
        return filter(status, priority, projectId, dueDateFrom, dueDateTo, keyword, assigneeId, allowedProjectIds, null, null);
    }

    public static Specification<Task> filter(
            TaskStatus status,
            TaskPriority priority,
            Long projectId,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            String keyword,
            Long assigneeId,
            List<Long> allowedProjectIds,
            String timeRange,
            String timeField
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }

            if (dueDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom));
            }

            if (dueDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueDateTo));
            }

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleMatch, descMatch));
            }

            if (assigneeId != null) {
                if (assigneeId == -1L || assigneeId == 0L) {
                    predicates.add(cb.isNull(root.get("assignee")));
                } else {
                    predicates.add(cb.equal(root.get("assignee").get("id"), assigneeId));
                }
            }

            if (allowedProjectIds != null) {
                if (allowedProjectIds.isEmpty()) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("project").get("id").in(allowedProjectIds));
                }
            }

            if (StringUtils.hasText(timeRange) && !"all".equalsIgnoreCase(timeRange.trim())) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime fromDate = switch (timeRange.trim().toLowerCase()) {
                    case "24h", "1d" -> now.minusHours(24);
                    case "3d" -> now.minusDays(3);
                    case "7d" -> now.minusDays(7);
                    case "30d" -> now.minusDays(30);
                    default -> null;
                };

                if (fromDate != null) {
                    String field = StringUtils.hasText(timeField) ? timeField.trim().toLowerCase() : "active";
                    switch (field) {
                        case "createdat", "created" -> predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
                        case "updatedat", "updated" -> predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), fromDate));
                        case "duedate", "due" -> predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), fromDate));
                        case "completedat", "completed" -> predicates.add(cb.greaterThanOrEqualTo(root.get("completedAt"), fromDate));
                        default -> predicates.add(cb.or(
                                cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate),
                                cb.greaterThanOrEqualTo(root.get("updatedAt"), fromDate)
                        ));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
