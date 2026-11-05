package com.portfolio.taskmanager.service;

import com.portfolio.taskmanager.dto.request.TaskCompleteRequest;
import com.portfolio.taskmanager.dto.request.TaskCreateRequest;
import com.portfolio.taskmanager.dto.response.PageResponse;
import com.portfolio.taskmanager.dto.response.TaskResponse;
import com.portfolio.taskmanager.dto.response.TaskScoreResponse;
import com.portfolio.taskmanager.entity.Project;
import com.portfolio.taskmanager.entity.Subtask;
import com.portfolio.taskmanager.entity.Task;
import com.portfolio.taskmanager.entity.User;
import com.portfolio.taskmanager.entity.enums.TaskPriority;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.entity.enums.UserRole;
import com.portfolio.taskmanager.exception.BadRequestException;
import com.portfolio.taskmanager.exception.ResourceNotFoundException;
import com.portfolio.taskmanager.mapper.SubtaskMapper;
import com.portfolio.taskmanager.mapper.TaskMapper;
import com.portfolio.taskmanager.repository.ProjectRepository;
import com.portfolio.taskmanager.repository.TaskRepository;
import com.portfolio.taskmanager.repository.UserRepository;
import com.portfolio.taskmanager.security.SecurityUtils;
import com.portfolio.taskmanager.service.impl.TaskServiceImpl;
import com.portfolio.taskmanager.service.smart.NaturalLanguageTaskParser;
import com.portfolio.taskmanager.service.smart.TaskPriorityCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Spy
    private SubtaskMapper subtaskMapper = new SubtaskMapper();

    @Spy
    private TaskMapper taskMapper = new TaskMapper(new SubtaskMapper());

    @Spy
    private TaskPriorityCalculator priorityCalculator = new TaskPriorityCalculator(new TaskMapper(new SubtaskMapper()));

    @Spy
    private NaturalLanguageTaskParser naturalLanguageTaskParser = new NaturalLanguageTaskParser();

    @InjectMocks
    private TaskServiceImpl taskService;

    private Project sampleProject;
    private Task sampleTask;
    private User adminUser;
    private User employeeUser;

    @BeforeEach
    void setUp() {
        sampleProject = Project.builder()
                .id(1L)
                .name("Hệ thống E-Commerce")
                .tasks(new ArrayList<>())
                .build();

        adminUser = User.builder().id(1L).username("admin").role(UserRole.ADMIN).build();
        employeeUser = User.builder().id(2L).username("dev_nam").fullName("Nam Dev").role(UserRole.EMPLOYEE).build();

        sampleTask = Task.builder()
                .id(10L)
                .title("Phát triển module giỏ hàng")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .project(sampleProject)
                .subtasks(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Tạo mới Task thành công kèm tự động tạo subtasks")
    void createTask_Success_WithSubtasks() {
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("Tích hợp cổng VnPay")
                .description("API tích hợp")
                .projectId(1L)
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .subtasks(List.of("Tạo tài khoản Sandbox", "Viết controller"))
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(adminUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(20L);
            return t;
        });

        TaskResponse response = taskService.createTask(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getTitle()).isEqualTo("Tích hợp cổng VnPay");
        assertThat(response.getSubtasks()).hasSize(2);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Tạo task thất bại do Project không tồn tại ném lỗi ResourceNotFoundException")
    void createTask_ProjectNotFound_ThrowsException() {
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("Task mới")
                .projectId(999L)
                .build();

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Dự án");
    }

    @Test
    @DisplayName("Task có subtask chưa hoàn thành 100% thì ném lỗi BadRequestException không cho chuyển DONE")
    void markTaskComplete_IncompleteSubtasks_ThrowsBadRequest() {
        Subtask s1 = Subtask.builder().id(101L).title("Việc 1").completed(true).task(sampleTask).build();
        Subtask s2 = Subtask.builder().id(102L).title("Việc 2").completed(false).task(sampleTask).build();
        sampleTask.addSubtask(s1);
        sampleTask.addSubtask(s2);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));

        TaskCompleteRequest completeRequest = TaskCompleteRequest.builder()
                .completionNote("Đã hoàn thành kiểm thử")
                .proofUrl("https://github.com/techcorp/ecommerce/pull/12")
                .build();

        assertThatThrownBy(() -> taskService.markTaskComplete(10L, completeRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("100%");
    }

    @Test
    @DisplayName("Task đã hoàn thành 100% subtask kèm minh chứng thì hoàn thành thành công")
    void markTaskComplete_AllSubtasksCompleted_Success() {
        Subtask s1 = Subtask.builder().id(101L).title("Việc 1").completed(true).task(sampleTask).build();
        Subtask s2 = Subtask.builder().id(102L).title("Việc 2").completed(true).task(sampleTask).build();
        sampleTask.addSubtask(s1);
        sampleTask.addSubtask(s2);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskCompleteRequest completeRequest = TaskCompleteRequest.builder()
                .completionNote("Đã hoàn thành kiểm thử và deploy")
                .proofUrl("https://github.com/techcorp/ecommerce/pull/12")
                .build();

        TaskResponse response = taskService.markTaskComplete(10L, completeRequest);

        assertThat(response.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(sampleTask.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(sampleTask.getCompletionNote()).isEqualTo("Đã hoàn thành kiểm thử và deploy");
        assertThat(sampleTask.getProofUrl()).isEqualTo("https://github.com/techcorp/ecommerce/pull/12");
        assertThat(sampleTask.getCompletedAt()).isNotNull();
        assertThat(response.getProgress()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Đánh dấu hoàn thành Task thiếu minh chứng sẽ ném BadRequestException")
    void markTaskComplete_WithoutProof_ThrowsBadRequest() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));

        assertThatThrownBy(() -> taskService.markTaskComplete(10L, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("minh chứng");

        TaskCompleteRequest emptyNoteRequest = TaskCompleteRequest.builder().completionNote("  ").build();
        assertThatThrownBy(() -> taskService.markTaskComplete(10L, emptyNoteRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("minh chứng");
    }

    @Test
    @DisplayName("Cập nhật trạng thái sang DONE mà chưa có minh chứng sẽ ném BadRequestException")
    void updateTaskStatus_ToDoneWithoutProof_ThrowsBadRequest() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));

        assertThatThrownBy(() -> taskService.updateTaskStatus(10L, TaskStatus.DONE))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("minh chứng");
    }

    @Test
    @DisplayName("Cập nhật trạng thái task sang IN_PROGRESS thành công")
    void updateTaskStatus_Success() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTaskStatus(10L, TaskStatus.IN_PROGRESS);

        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(sampleTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Cập nhật trạng thái task sang REWORK kèm ghi chú phản hồi lỗi thành công")
    void updateTaskStatus_ToReworkWithNote_Success() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTaskStatus(10L, TaskStatus.REWORK, "Khách hàng báo lỗi tràn màn hình");

        assertThat(response.getStatus()).isEqualTo(TaskStatus.REWORK);
        assertThat(sampleTask.getStatus()).isEqualTo(TaskStatus.REWORK);
        assertThat(sampleTask.getReworkNote()).isEqualTo("Khách hàng báo lỗi tràn màn hình");
    }

    @Test
    @DisplayName("Lấy danh sách smart priority sắp xếp giảm dần theo điểm ưu tiên")
    void getSmartPriorityTasks_SortedDesc() {
        Task lowTask = Task.builder()
                .id(1L)
                .title("Việc nhẹ")
                .priority(TaskPriority.LOW)
                .status(TaskStatus.DONE)
                .project(sampleProject)
                .build();

        Task urgentTask = Task.builder()
                .id(2L)
                .title("Fix bug sập server")
                .priority(TaskPriority.URGENT)
                .status(TaskStatus.IN_PROGRESS)
                .dueDate(LocalDateTime.now().minusDays(1))
                .project(sampleProject)
                .build();

        when(taskRepository.findAll()).thenReturn(List.of(lowTask, urgentTask));

        List<TaskScoreResponse> scores = taskService.getSmartPriorityTasks();

        assertThat(scores).hasSize(2);
        assertThat(scores.get(0).getTask().getId()).isEqualTo(2L);
        assertThat(scores.get(1).getTask().getId()).isEqualTo(1L);
        assertThat(scores.get(0).getTotalScore()).isGreaterThan(scores.get(1).getTotalScore());
    }

    @Test
    @DisplayName("Admin phân công việc cho nhân viên thành công")
    void assignTask_ByAdmin_Success() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(employeeUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = taskService.assignTask(10L, 2L);

        assertThat(response).isNotNull();
        assertThat(sampleTask.getAssignee()).isEqualTo(employeeUser);
    }

    @Test
    @DisplayName("Nhân viên tự nhận việc cho chính mình thành công")
    void assignTask_ByEmployeeSelfAssign_Success() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(employeeUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(employeeUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = taskService.assignTask(10L, null); // null = tự nhận

        assertThat(response).isNotNull();
        assertThat(sampleTask.getAssignee()).isEqualTo(employeeUser);
    }

    @Test
    @DisplayName("Nhân viên cố giao việc cho người khác bị chặn AccessDeniedException")
    void assignTask_ByEmployeeToOther_ThrowsAccessDenied() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(employeeUser));

        assertThatThrownBy(() -> taskService.assignTask(10L, 3L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ Quản trị viên");
    }

    @Test
    @DisplayName("Lấy danh sách việc của tôi (My Tasks) thành công")
    void getMyTasks_Success() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(employeeUser));
        Page<Task> page = new PageImpl<>(List.of(sampleTask));
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<TaskResponse> response = taskService.getMyTasks(PageRequest.of(0, 10));

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
    }
}
