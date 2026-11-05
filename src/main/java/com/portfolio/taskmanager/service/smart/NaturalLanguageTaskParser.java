package com.portfolio.taskmanager.service.smart;

import com.portfolio.taskmanager.entity.enums.TaskPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bộ phân tích cú pháp ngôn ngữ tự nhiên (NLP Regex Parser hỗ trợ tiếng Việt Unicode đầy đủ).
 * Trích xuất ngày, giờ, mức độ ưu tiên và tiêu đề công việc từ câu nói tiếng Việt / tiếng Anh.
 */
@Component
public class NaturalLanguageTaskParser {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParsedTaskResult {
        private String title;
        private LocalDateTime dueDate;
        private TaskPriority priority;
    }

    private static final Pattern URGENT_PATTERN = Pattern.compile("(?i)(?<!\\p{L})(urgent|khẩn cấp|rất gấp|gấp)(?!\\p{L})");
    private static final Pattern HIGH_PATTERN = Pattern.compile("(?i)(?<!\\p{L})(high|quan trọng|ưu tiên cao|cao)(?!\\p{L})");
    private static final Pattern LOW_PATTERN = Pattern.compile("(?i)(?<!\\p{L})(low|thấp|không gấp)(?!\\p{L})");

    private static final Pattern RELATIVE_DATE_TODAY = Pattern.compile("(?i)(?<!\\p{L})(hôm nay|today)(?!\\p{L})");
    private static final Pattern RELATIVE_DATE_TOMORROW = Pattern.compile("(?i)(?<!\\p{L})(ngày mai|mai|tomorrow)(?!\\p{L})");
    private static final Pattern RELATIVE_DATE_DAY_AFTER = Pattern.compile("(?i)(?<!\\p{L})(ngày kia|ngày mốt)(?!\\p{L})");

    private static final Pattern ABSOLUTE_DATE_PATTERN = Pattern.compile("(?<!\\p{L})(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{4}))?(?!\\p{L})");
    private static final Pattern TIME_PATTERN = Pattern.compile("(?i)(?:lúc\\s+|vào\\s+)?(\\d{1,2})(?:[:h](\\d{2})|h(?:\\s|$))");

    private static final String[] STOP_WORDS = {
            "mức độ", "độ ưu tiên", "ưu tiên", "hạn chót", "deadline", "trước",
            "vào lúc", "lúc", "vào ngày", "vào", "ngày", "hôm"
    };

    public ParsedTaskResult parse(String input) {
        if (!StringUtils.hasText(input)) {
            return ParsedTaskResult.builder()
                    .title("Công việc mới")
                    .dueDate(null)
                    .priority(TaskPriority.MEDIUM)
                    .build();
        }

        String workingText = input;

        // 1. Nhận diện Mức độ ưu tiên
        TaskPriority priority = TaskPriority.MEDIUM;
        Matcher urgentMatcher = URGENT_PATTERN.matcher(workingText);
        Matcher highMatcher = HIGH_PATTERN.matcher(workingText);
        Matcher lowMatcher = LOW_PATTERN.matcher(workingText);

        if (urgentMatcher.find()) {
            priority = TaskPriority.URGENT;
            workingText = urgentMatcher.replaceAll("");
        } else if (highMatcher.find()) {
            priority = TaskPriority.HIGH;
            workingText = highMatcher.replaceAll("");
        } else if (lowMatcher.find()) {
            priority = TaskPriority.LOW;
            workingText = lowMatcher.replaceAll("");
        }

        // 2. Nhận diện Ngày (Date)
        LocalDate targetDate = null;
        Matcher tomorrowMatcher = RELATIVE_DATE_TOMORROW.matcher(workingText);
        Matcher dayAfterMatcher = RELATIVE_DATE_DAY_AFTER.matcher(workingText);
        Matcher todayMatcher = RELATIVE_DATE_TODAY.matcher(workingText);
        Matcher absDateMatcher = ABSOLUTE_DATE_PATTERN.matcher(workingText);

        if (tomorrowMatcher.find()) {
            targetDate = LocalDate.now().plusDays(1);
            workingText = tomorrowMatcher.replaceAll("");
        } else if (dayAfterMatcher.find()) {
            targetDate = LocalDate.now().plusDays(2);
            workingText = dayAfterMatcher.replaceAll("");
        } else if (todayMatcher.find()) {
            targetDate = LocalDate.now();
            workingText = todayMatcher.replaceAll("");
        } else if (absDateMatcher.find()) {
            try {
                int day = Integer.parseInt(absDateMatcher.group(1));
                int month = Integer.parseInt(absDateMatcher.group(2));
                int year = absDateMatcher.group(3) != null ? Integer.parseInt(absDateMatcher.group(3)) : LocalDate.now().getYear();
                targetDate = LocalDate.of(year, month, day);
                workingText = absDateMatcher.replaceAll("");
            } catch (Exception ignored) {
                // Ignore parse errors, keep targetDate null
            }
        }

        // 3. Nhận diện Giờ (Time)
        LocalTime targetTime = null;
        Matcher timeMatcher = TIME_PATTERN.matcher(workingText);
        if (timeMatcher.find()) {
            try {
                int hour = Integer.parseInt(timeMatcher.group(1));
                int minute = timeMatcher.group(2) != null ? Integer.parseInt(timeMatcher.group(2)) : 0;
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    targetTime = LocalTime.of(hour, minute);
                    workingText = timeMatcher.replaceAll("");
                }
            } catch (Exception ignored) {
                // Ignore parse errors
            }
        }

        // Kết hợp Date & Time
        LocalDateTime dueDateTime = null;
        if (targetDate != null && targetTime != null) {
            dueDateTime = LocalDateTime.of(targetDate, targetTime);
        } else if (targetDate != null) {
            dueDateTime = LocalDateTime.of(targetDate, LocalTime.of(18, 0)); // Mặc định 18:00
        } else if (targetTime != null) {
            LocalDate baseDate = LocalTime.now().isAfter(targetTime) ? LocalDate.now().plusDays(1) : LocalDate.now();
            dueDateTime = LocalDateTime.of(baseDate, targetTime);
        }

        // 4. Chuẩn hóa tiêu đề còn lại (loại bỏ các từ nối và bổ nghĩa tiếng Việt)
        String cleanedTitle = workingText;
        for (String stopWord : STOP_WORDS) {
            cleanedTitle = cleanedTitle.replaceAll("(?i)(?<!\\p{L})" + Pattern.quote(stopWord) + "(?!\\p{L})", " ");
        }

        cleanedTitle = cleanedTitle
                .replaceAll("[\\[\\]()\\-_,:;]+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleanedTitle.isEmpty()) {
            cleanedTitle = "Công việc mới từ ghi chú";
        }

        return ParsedTaskResult.builder()
                .title(cleanedTitle)
                .dueDate(dueDateTime)
                .priority(priority)
                .build();
    }
}
