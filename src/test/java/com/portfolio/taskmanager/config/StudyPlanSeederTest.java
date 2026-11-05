package com.portfolio.taskmanager.config;

import com.portfolio.taskmanager.entity.Project;
import com.portfolio.taskmanager.entity.Task;
import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.repository.ProjectRepository;
import com.portfolio.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class StudyPlanSeederTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StudyPlanSeeder studyPlanSeeder;

    @Test
    @Transactional
    @DisplayName("StudyPlanSeeder tạo đúng dự án 14 ngày ôn tập với cấu hình chính xác cho admin")
    void studyPlanSeeder_Seeds14TasksWithCorrectProperties() {
        Optional<Project> optProject = projectRepository.findByNameIgnoreCase(StudyPlanSeeder.PROJECT_NAME);
        assertThat(optProject).isPresent();

        Project project = optProject.get();
        List<Task> tasks = taskRepository.findByProjectId(project.getId());
        assertThat(tasks).hasSize(14);

        // Sắp xếp theo hạn chót để kiểm tra tuần tự 14 ngày
        tasks.sort((a, b) -> a.getDueDate().compareTo(b.getDueDate()));

        for (int i = 0; i < 14; i++) {
            int dayNumber = i + 1;
            Task task = tasks.get(i);

            // Tiêu đề bắt đầu bằng "Ngày X: "
            assertThat(task.getTitle()).startsWith("Ngày " + dayNumber + ": ");

            // Assignee và Creator là admin
            assertThat(task.getAssignee()).isNotNull();
            assertThat(task.getAssignee().getUsername()).isEqualTo("admin");
            assertThat(task.getCreator()).isNotNull();
            assertThat(task.getCreator().getUsername()).isEqualTo("admin");

            // Status là TODO
            assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);

            // Hạn chót lúc 21:00 ngày (06/11/2026 + i ngày)
            LocalDateTime expectedDueDate = LocalDateTime.of(2026, 11, 6 + i, 21, 0, 0);
            assertThat(task.getDueDate()).isEqualTo(expectedDueDate);

            // Mức độ ưu tiên: Ngày 14 là HIGH, các ngày 1-13 là MEDIUM
            if (dayNumber == 14) {
                assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);
            } else {
                assertThat(task.getPriority()).isEqualTo(TaskPriority.MEDIUM);
            }

            // Mỗi task có từ 3 đến 5 subtasks, tất cả đều chưa hoàn thành
            assertThat(task.getSubtasks()).hasSizeBetween(3, 5);
            assertThat(task.getSubtasks()).allMatch(subtask -> !subtask.isCompleted());
        }
    }

    @Test
    @DisplayName("StudyPlanSeeder đảm bảo tính Idempotent: chạy lại không nhân đôi dự án hoặc công việc")
    void studyPlanSeeder_IsIdempotent_DoesNotDuplicate() {
        long initialProjects = projectRepository.count();
        long initialTasks = taskRepository.count();

        // Chạy lại seeder lần 2
        studyPlanSeeder.seedStudyPlan();

        // Số lượng project và task không được thay đổi
        assertThat(projectRepository.count()).isEqualTo(initialProjects);
        assertThat(taskRepository.count()).isEqualTo(initialTasks);
    }

    @Test
    @DisplayName("API GET /api/v1/tasks/my-tasks của admin trả về đủ 14 task ôn tập đúng hạn chót và priority")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void apiGetMyTasks_AsAdmin_ReturnsAll14StudyTasks() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/my-tasks")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "dueDate")
                        .param("sortDir", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(14))
                .andExpect(jsonPath("$.data.content", hasSize(14)))
                .andExpect(jsonPath("$.data.content[0].title").value("Ngày 1: Nguồn gốc và khái niệm nhà nước"))
                .andExpect(jsonPath("$.data.content[0].priority").value("MEDIUM"))
                .andExpect(jsonPath("$.data.content[0].status").value("TODO"))
                .andExpect(jsonPath("$.data.content[0].dueDate").value("2026-11-06T21:00:00"))
                .andExpect(jsonPath("$.data.content[13].title").value("Ngày 14: Ôn tập tổng hợp và kiểm tra"))
                .andExpect(jsonPath("$.data.content[13].priority").value("HIGH"))
                .andExpect(jsonPath("$.data.content[13].status").value("TODO"))
                .andExpect(jsonPath("$.data.content[13].dueDate").value("2026-11-19T21:00:00"));
    }
}

