package com.portfolio.taskmanager.service.smart;

import com.portfolio.taskmanager.entity.enums.TaskPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class NaturalLanguageTaskParserTest {

    private NaturalLanguageTaskParser parser;

    @BeforeEach
    void setUp() {
        parser = new NaturalLanguageTaskParser();
    }

    @Test
    @DisplayName("Parse câu 'Họp khách hàng lúc 14:00 ngày mai' thành công")
    void shouldParseTomorrowTimeCorrectly() {
        String input = "Họp khách hàng lúc 14:00 ngày mai";
        NaturalLanguageTaskParser.ParsedTaskResult result = parser.parse(input);

        assertThat(result.getTitle()).isEqualTo("Họp khách hàng");
        assertThat(result.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(result.getDueDate()).isNotNull();
        assertThat(result.getDueDate().toLocalDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(result.getDueDate().getHour()).isEqualTo(14);
        assertThat(result.getDueDate().getMinute()).isZero();
    }

    @Test
    @DisplayName("Parse câu có từ khóa khẩn cấp URGENT và định dạng giờ 17h")
    void shouldParseUrgentAndHourFormat() {
        String input = "Nộp báo cáo tài chính gấp lúc 17h hôm nay";
        NaturalLanguageTaskParser.ParsedTaskResult result = parser.parse(input);

        assertThat(result.getTitle()).contains("Nộp báo cáo tài chính");
        assertThat(result.getPriority()).isEqualTo(TaskPriority.URGENT);
        assertThat(result.getDueDate()).isNotNull();
        assertThat(result.getDueDate().toLocalDate()).isEqualTo(LocalDate.now());
        assertThat(result.getDueDate().getHour()).isEqualTo(17);
    }

    @Test
    @DisplayName("Parse câu có ngày tuyệt đối dd/MM/yyyy và từ khóa HIGH")
    void shouldParseAbsoluteDateAndHighPriority() {
        String input = "Review pull request backend quan trọng vào 25/12/2026";
        NaturalLanguageTaskParser.ParsedTaskResult result = parser.parse(input);

        assertThat(result.getTitle()).contains("Review pull request backend");
        assertThat(result.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(result.getDueDate()).isNotNull();
        assertThat(result.getDueDate().toLocalDate()).isEqualTo(LocalDate.of(2026, 12, 25));
        assertThat(result.getDueDate().getHour()).isEqualTo(18); // Default time
    }

    @Test
    @DisplayName("Chuỗi rỗng trả về kết quả mặc định an toàn")
    void shouldHandleEmptyInputSafely() {
        NaturalLanguageTaskParser.ParsedTaskResult result = parser.parse("");

        assertThat(result.getTitle()).isNotEmpty();
        assertThat(result.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(result.getDueDate()).isNull();
    }
}

