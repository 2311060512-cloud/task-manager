package com.portfolio.taskmanager.service.impl;

import com.portfolio.taskmanager.dto.request.ProjectCreateRequest;
import com.portfolio.taskmanager.dto.request.ProjectUpdateRequest;
import com.portfolio.taskmanager.dto.response.ProjectDetailResponse;
import com.portfolio.taskmanager.dto.response.ProjectResponse;
import com.portfolio.taskmanager.entity.Project;
import com.portfolio.taskmanager.entity.User;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.entity.enums.UserRole;
import com.portfolio.taskmanager.exception.BadRequestException;
import com.portfolio.taskmanager.exception.ResourceNotFoundException;
import com.portfolio.taskmanager.mapper.ProjectMapper;
import com.portfolio.taskmanager.repository.ProjectRepository;
import com.portfolio.taskmanager.repository.TaskRepository;
import com.portfolio.taskmanager.security.SecurityUtils;
import com.portfolio.taskmanager.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMapper projectMapper;
    private final SecurityUtils securityUtils;

    @Override
    public ProjectResponse createProject(ProjectCreateRequest request) {
        if (projectRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Dự án với tên '" + request.getName().trim() + "' đã tồn tại");
        }

        Project project = Project.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        List<Project> projects;
        Optional<User> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isPresent() && currentUser.get().getRole() == UserRole.CLIENT) {
            projects = projectRepository.findByClientId(currentUser.get().getId());
        } else {
            projects = projectRepository.findAll();
        }
        return projectMapper.toResponseList(projects);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dự án", "id", id));

        Optional<User> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isPresent() && currentUser.get().getRole() == UserRole.CLIENT) {
            if (project.getClient() == null || !project.getClient().getId().equals(currentUser.get().getId())) {
                throw new AccessDeniedException("Khách hàng chỉ có quyền xem chi tiết dự án của mình");
            }
        }

        return projectMapper.toDetailResponse(project);
    }

    @Override
    public ProjectResponse updateProject(Long id, ProjectUpdateRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dự án", "id", id));

        String newName = request.getName().trim();
        if (projectRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {
            throw new BadRequestException("Dự án với tên '" + newName + "' đã tồn tại");
        }

        project.setName(newName);
        project.setDescription(request.getDescription());

        Project updated = projectRepository.save(project);
        return projectMapper.toResponse(updated);
    }

    @Override
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dự án", "id", id));

        long inProgressCount = taskRepository.countByProjectIdAndStatus(id, TaskStatus.IN_PROGRESS);
        long reworkCount = taskRepository.countByProjectIdAndStatus(id, TaskStatus.REWORK);
        long activeCount = inProgressCount + reworkCount;
        if (activeCount > 0) {
            throw new BadRequestException("Không thể xóa dự án khi vẫn còn " + activeCount + " công việc đang thực hiện dở dang hoặc cần sửa lỗi (IN_PROGRESS, REWORK). Vui lòng hoàn thành hoặc hủy các công việc này trước khi xóa.");
        }

        projectRepository.delete(project);
    }
}

