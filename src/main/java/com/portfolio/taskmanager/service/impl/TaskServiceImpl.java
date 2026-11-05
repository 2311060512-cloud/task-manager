package com.portfolio.taskmanager.service.impl;

import com.portfolio.taskmanager.dto.request.QuickTaskRequest;
import com.portfolio.taskmanager.dto.request.TaskCompleteRequest;
import com.portfolio.taskmanager.dto.request.TaskCreateRequest;
import com.portfolio.taskmanager.dto.request.TaskUpdateRequest;
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
import com.portfolio.taskmanager.mapper.TaskMapper;
import com.portfolio.taskmanager.repository.ProjectRepository;
import com.portfolio.taskmanager.repository.TaskRepository;
import com.portfolio.taskmanager.repository.UserRepository;
import com.portfolio.taskmanager.repository.specification.TaskSpecification;
import com.portfolio.taskmanager.security.SecurityUtils;
import com.portfolio.taskmanager.service.TaskService;
import com.portfolio.taskmanager.service.smart.NaturalLanguageTaskParser;
import com.portfolio.taskmanager.service.smart.TaskPriorityCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final TaskPriorityCalculator taskPriorityCalculator;
    private final NaturalLanguageTaskParser naturalLanguageTaskParser;
    private final SecurityUtils securityUtils;

    @Override
    public TaskResponse createTask(TaskCreateRequest request) {
        Project project;
        Optional<User> currentUserOpt = securityUtils.getCurrentUser();
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dự án", "id", request.getProjectId()));
        } else if (currentUserOpt.isPresent() && currentUserOpt.get().getRole() == UserRole.CLIENT) {
            project = projectRepository.findByClientId(currentUserOpt.get().getId()).stream()
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Khách hàng chưa có dự án nào được gán để tạo công việc"));
        } else {
            project = projectRepository.findAll().stream()
                    .findFirst()
                    .orElseGet(() -> projectRepository.save(Project.builder()
                            .name("Công việc chung")
                            .description("Dự án mặc định cho các công việc")
                            .build()));
        }

        Task task = Task.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .project(project)
                .build();

        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            task.setCreator(currentUser);

            if (currentUser.getRole() == UserRole.CLIENT) {
                if (project.getClient() == null || !project.getClient().getId().equals(currentUser.getId())) {
                    throw new BadRequestException("Khách hàng chỉ có thể tạo yêu cầu trong dự án của chính mình");
                }
            }
        }

        if (request.getSubtasks() != null) {
            for (String subtaskTitle : request.getSubtasks()) {
                if (StringUtils.hasText(subtaskTitle)) {
                    task.addSubtask(Subtask.builder()
                            .title(subtaskTitle.trim())
                            .completed(false)
                            .build());
                }
            }
        }

        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getTasks(
            TaskStatus status,
            TaskPriority priority,
            Long projectId,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            String keyword,
            Pageable pageable
    ) {
        return getTasks(status, priority, projectId, dueDateFrom, dueDateTo, keyword, null, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getTasks(
            TaskStatus status,
            TaskPriority priority,
            Long projectId,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            String keyword,
            Long assigneeId,
            Pageable pageable
    ) {
        return getTasks(status, priority, projectId, dueDateFrom, dueDateTo, keyword, assigneeId, null, null, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getTasks(
            TaskStatus status,
            TaskPriority priority,
            Long projectId,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            String keyword,
            Long assigneeId,
            String timeRange,
            String timeField,
            Pageable pageable
    ) {
        List<Long> allowedProjectIds = null;
        Long effectiveAssigneeId = assigneeId;

        Optional<User> currentUserOpt = securityUtils.getCurrentUser();
        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            if (currentUser.getRole() == UserRole.CLIENT) {
                allowedProjectIds = projectRepository.findByClientId(currentUser.getId())
                        .stream()
                        .map(Project::getId)
                        .toList();
            } else if (currentUser.getRole() == UserRole.EMPLOYEE) {
                // Tài khoản nhân viên chỉ xem được công việc được giao cho chính mình
                effectiveAssigneeId = currentUser.getId();
            }
        }

        Specification<Task> spec = TaskSpecification.filter(
                status, priority, projectId, dueDateFrom, dueDateTo, keyword, effectiveAssigneeId, allowedProjectIds, timeRange, timeField
        );
        Page<Task> taskPage = taskRepository.findAll(spec, pageable);
        Page<TaskResponse> responsePage = taskPage.map(taskMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getMyTasks(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Vui lòng đăng nhập để xem công việc được giao của bạn"));
        return getTasks(null, null, null, null, null, null, currentUser.getId(), pageable);
    }

    @Override
    public TaskResponse assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", taskId));

        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Vui lòng đăng nhập để phân công công việc"));

        Long targetUserId = userId != null ? userId : currentUser.getId();

        if (currentUser.getRole() != UserRole.ADMIN && !targetUserId.equals(currentUser.getId())) {
            throw new AccessDeniedException("Chỉ Quản trị viên mới có quyền giao việc cho người khác");
        }

        User assignee = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", targetUserId));

        if (assignee.getRole() == UserRole.CLIENT) {
            throw new BadRequestException("Không thể phân công công việc kỹ thuật cho Khách hàng");
        }

        task.setAssignee(assignee);
        Task updated = taskRepository.save(task);
        return taskMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", id));

        Optional<User> currentUserOpt = securityUtils.getCurrentUser();
        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            if (currentUser.getRole() == UserRole.EMPLOYEE) {
                boolean isAssignee = task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId());
                boolean isCreator = task.getCreator() != null && task.getCreator().getId().equals(currentUser.getId());
                if (!isAssignee && !isCreator) {
                    throw new AccessDeniedException("Bạn chỉ có quyền xem chi tiết công việc của chính mình");
                }
            } else if (currentUser.getRole() == UserRole.CLIENT) {
                if (task.getProject().getClient() == null || !task.getProject().getClient().getId().equals(currentUser.getId())) {
                    throw new AccessDeniedException("Bạn chỉ có quyền xem công việc thuộc dự án của mình");
                }
            }
        }

        return taskMapper.toResponse(task);
    }

    @Override
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", id));

        Optional<User> currentUserOpt = securityUtils.getCurrentUser();
        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            if (currentUser.getRole() == UserRole.EMPLOYEE) {
                boolean isAssigneeOrCreator = (task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId()))
                        || (task.getCreator() != null && task.getCreator().getId().equals(currentUser.getId()));
                if (!isAssigneeOrCreator) {
                    throw new AccessDeniedException("Nhân viên chỉ có quyền chỉnh sửa công việc của chính mình hoặc được phân công");
                }
            } else if (currentUser.getRole() == UserRole.CLIENT) {
                throw new AccessDeniedException("Khách hàng không có quyền chỉnh sửa trực tiếp công việc kỹ thuật");
            }
        }

        if (!task.getProject().getId().equals(request.getProjectId())) {
            Project newProject = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dự án", "id", request.getProjectId()));
            task.setProject(newProject);
        }

        if (request.getStatus() == TaskStatus.DONE && task.getStatus() != TaskStatus.DONE) {
            if (task.getSubtasks() != null && !task.getSubtasks().isEmpty()) {
                boolean allDone = task.getSubtasks().stream().allMatch(Subtask::isCompleted);
                if (!allDone) {
                    throw new BadRequestException("Không thể cập nhật sang trạng thái HOÀN TẤT khi chưa hoàn thành 100% các công việc con (subtasks)");
                }
            }
            if (!StringUtils.hasText(task.getCompletionNote())) {
                throw new BadRequestException("Không thể chuyển trạng thái sang HOÀN TẤT khi chưa nộp minh chứng công việc. Vui lòng sử dụng chức năng nộp minh chứng hoàn thành.");
            }
        } else if (request.getStatus() != TaskStatus.DONE && task.getStatus() == TaskStatus.DONE) {
            task.setCompletedAt(null);
            task.setCompletedBy(null);
            task.setCompletionNote(null);
            task.setProofUrl(null);
        }

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());

        Task updated = taskRepository.save(task);
        return taskMapper.toResponse(updated);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", id));
        taskRepository.delete(task);
    }

    @Override
    public TaskResponse markTaskComplete(Long id, TaskCompleteRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", id));

        Optional<User> currentUserOpt = securityUtils.getCurrentUser();
        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            if (currentUser.getRole() == UserRole.EMPLOYEE) {
                boolean isAssigneeOrCreator = (task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId()))
                        || (task.getCreator() != null && task.getCreator().getId().equals(currentUser.getId()));
                if (!isAssigneeOrCreator) {
                    throw new AccessDeniedException("Nhân viên chỉ có quyền hoàn thành công việc của chính mình hoặc được phân công");
                }
            } else if (currentUser.getRole() == UserRole.CLIENT) {
                throw new AccessDeniedException("Khách hàng không có quyền hoàn thành trực tiếp công việc kỹ thuật");
            }
        }

        if (task.getSubtasks() != null && !task.getSubtasks().isEmpty()) {
            boolean allDone = task.getSubtasks().stream().allMatch(Subtask::isCompleted);
            if (!allDone) {
                throw new BadRequestException("Không thể hoàn thành công việc: Tất cả công việc con (subtasks) phải hoàn thành 100% trước khi chuyển sang HOÀN TẤT");
            }
        }

        if (request == null || !StringUtils.hasText(request.getCompletionNote())) {
            throw new BadRequestException("Bắt buộc phải cung cấp minh chứng hoàn thành công việc (nội dung báo cáo hoặc link/ảnh minh chứng)");
        }

        User currentUser = securityUtils.getCurrentUser().orElse(null);
        task.markAsCompleted(
                request.getCompletionNote().trim(),
                StringUtils.hasText(request.getProofUrl()) ? request.getProofUrl().trim() : null,
                currentUser
        );
        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse markTaskComplete(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", id));

        if (task.getSubtasks() != null && !task.getSubtasks().isEmpty()) {
            boolean allDone = task.getSubtasks().stream().allMatch(Subtask::isCompleted);
            if (!allDone) {
                throw new BadRequestException("Không thể hoàn thành công việc: Tất cả công việc con (subtasks) phải hoàn thành 100% trước khi chuyển sang HOÀN TẤT");
            }
        }

        if (!StringUtils.hasText(task.getCompletionNote())) {
            throw new BadRequestException("Bắt buộc phải cung cấp minh chứng hoàn thành công việc (nội dung báo cáo hoặc link/ảnh minh chứng)");
        }

        task.markAsCompleted();
        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse updateTaskStatus(Long id, TaskStatus status) {
        return updateTaskStatus(id, status, null);
    }

    @Override
    public TaskResponse updateTaskStatus(Long id, TaskStatus status, String reworkNote) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", id));

        Optional<User> currentUserOpt = securityUtils.getCurrentUser();
        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            if (currentUser.getRole() == UserRole.CLIENT) {
                boolean isClientProject = task.getProject() != null
                        && task.getProject().getClient() != null
                        && task.getProject().getClient().getId().equals(currentUser.getId());
                if (!isClientProject) {
                    throw new AccessDeniedException("Khách hàng chỉ có thể thao tác trên các công việc thuộc dự án của mình");
                }
                if (status != TaskStatus.REWORK) {
                    throw new AccessDeniedException("Khách hàng chỉ có quyền yêu cầu sửa lại (REWORK) công việc");
                }
            } else if (currentUser.getRole() == UserRole.EMPLOYEE) {
                boolean isAssigneeOrCreator = (task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId()))
                        || (task.getCreator() != null && task.getCreator().getId().equals(currentUser.getId()));
                if (!isAssigneeOrCreator) {
                    throw new AccessDeniedException("Nhân viên chỉ có quyền cập nhật trạng thái công việc được phân công");
                }
            }
        }

        if (status == TaskStatus.DONE) {
            if (task.getSubtasks() != null && !task.getSubtasks().isEmpty()) {
                boolean allDone = task.getSubtasks().stream().allMatch(Subtask::isCompleted);
                if (!allDone) {
                    throw new BadRequestException("Không thể chuyển sang trạng thái HOÀN TẤT: Tất cả công việc con (subtasks) phải hoàn thành 100%");
                }
            }
            if (!StringUtils.hasText(task.getCompletionNote())) {
                throw new BadRequestException("Không thể chuyển trực tiếp sang trạng thái HOÀN TẤT. Vui lòng nộp minh chứng công việc qua chức năng hoàn thành");
            }
            task.markAsCompleted();
        } else if (status == TaskStatus.REWORK) {
            task.setStatus(TaskStatus.REWORK);
            task.setCompletedAt(null);
            task.setCompletedBy(null);
            task.setCompletionNote(null);
            task.setProofUrl(null);
            if (StringUtils.hasText(reworkNote)) {
                task.setReworkNote(reworkNote.trim());
            }
        } else {
            task.setStatus(status);
            if (task.getCompletedAt() != null) {
                task.setCompletedAt(null);
                task.setCompletedBy(null);
                task.setCompletionNote(null);
                task.setProofUrl(null);
            }
        }

        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskScoreResponse> getSmartPriorityTasks() {
        List<Task> tasks;
        Optional<User> currentUserOpt = securityUtils.getCurrentUser();
        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            if (currentUser.getRole() == UserRole.EMPLOYEE) {
                tasks = taskRepository.findAll().stream()
                        .filter(t -> (t.getAssignee() != null && t.getAssignee().getId().equals(currentUser.getId()))
                                || (t.getCreator() != null && t.getCreator().getId().equals(currentUser.getId())))
                        .toList();
            } else if (currentUser.getRole() == UserRole.CLIENT) {
                List<Long> clientProjectIds = projectRepository.findByClientId(currentUser.getId())
                        .stream().map(Project::getId).toList();
                tasks = taskRepository.findAll().stream()
                        .filter(t -> t.getProject() != null && clientProjectIds.contains(t.getProject().getId()))
                        .toList();
            } else {
                tasks = taskRepository.findAll();
            }
        } else {
            tasks = taskRepository.findAll();
        }

        return tasks.stream()
                .map(taskPriorityCalculator::calculateScore)
                .sorted(Comparator.comparingInt(TaskScoreResponse::getTotalScore).reversed())
                .toList();
    }

    @Override
    public TaskResponse createQuickTask(QuickTaskRequest request) {
        NaturalLanguageTaskParser.ParsedTaskResult parsed = naturalLanguageTaskParser.parse(request.getText());

        Project project;
        Optional<User> currentUserOpt = securityUtils.getCurrentUser();
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dự án", "id", request.getProjectId()));
        } else if (currentUserOpt.isPresent() && currentUserOpt.get().getRole() == UserRole.CLIENT) {
            project = projectRepository.findByClientId(currentUserOpt.get().getId()).stream()
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Khách hàng chưa có dự án nào được gán để tạo công việc"));
        } else {
            project = projectRepository.findAll().stream()
                    .findFirst()
                    .orElseGet(() -> projectRepository.save(Project.builder()
                            .name("Công việc chung")
                            .description("Dự án mặc định cho các công việc tạo nhanh")
                            .build()));
        }

        Task task = Task.builder()
                .title(parsed.getTitle())
                .description("Tạo tự động từ câu lệnh tự nhiên: \"" + request.getText() + "\"")
                .dueDate(parsed.getDueDate())
                .priority(parsed.getPriority())
                .status(TaskStatus.TODO)
                .project(project)
                .build();

        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            task.setCreator(currentUser);

            if (currentUser.getRole() == UserRole.CLIENT) {
                if (project.getClient() == null || !project.getClient().getId().equals(currentUser.getId())) {
                    throw new BadRequestException("Khách hàng chỉ có thể tạo yêu cầu trong dự án của chính mình");
                }
            }
        }

        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }
}

