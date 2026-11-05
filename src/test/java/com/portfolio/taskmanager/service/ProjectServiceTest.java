package com.portfolio.taskmanager.service;

import com.portfolio.taskmanager.dto.request.ProjectCreateRequest;
import com.portfolio.taskmanager.dto.request.ProjectUpdateRequest;
import com.portfolio.taskmanager.dto.response.ProjectDetailResponse;
import com.portfolio.taskmanager.dto.response.ProjectResponse;
import com.portfolio.taskmanager.entity.Project;
import com.portfolio.taskmanager.entity.User;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.entity.enums.UserRole;
import com.portfolio.taskmanager.exception.BadRequestException;
import com.portfolio.taskmanager.mapper.ProjectMapper;
import com.portfolio.taskmanager.mapper.SubtaskMapper;
import com.portfolio.taskmanager.mapper.TaskMapper;
import com.portfolio.taskmanager.repository.ProjectRepository;
import com.portfolio.taskmanager.repository.TaskRepository;
import com.portfolio.taskmanager.security.SecurityUtils;
import com.portfolio.taskmanager.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Spy
    private ProjectMapper projectMapper = new ProjectMapper(new TaskMapper(new SubtaskMapper()));

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project sampleProject;

    @BeforeEach
    void setUp() {
        sampleProject = Project.builder()
                .id(1L)
                .name("Hệ thống ERP Doanh Nghiệp")
                .description("Phần mềm quản trị nội bộ")
                .tasks(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("createProject - Tạo mới dự án thành công")
    void createProject_Success() {
        ProjectCreateRequest request = ProjectCreateRequest.builder()
                .name("Dự án Mới")
                .description("Mô tả")
                .build();

        when(projectRepository.existsByNameIgnoreCase("Dự án Mới")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(sampleProject);

        ProjectResponse response = projectService.createProject(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("createProject - Báo lỗi BadRequest khi tên dự án bị trùng")
    void createProject_DuplicateName_ThrowsException() {
        ProjectCreateRequest request = ProjectCreateRequest.builder()
                .name("Hệ thống ERP Doanh Nghiệp")
                .build();

        when(projectRepository.existsByNameIgnoreCase("Hệ thống ERP Doanh Nghiệp")).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đã tồn tại");
    }

    @Test
    @DisplayName("getAllProjects - Lấy danh sách dự án cho Admin thành công")
    void getAllProjects_Success() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());
        when(projectRepository.findAll()).thenReturn(List.of(sampleProject));

        List<ProjectResponse> result = projectService.getAllProjects();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Hệ thống ERP Doanh Nghiệp");
    }

    @Test
    @DisplayName("getAllProjects - Khách hàng chỉ xem được dự án của mình")
    void getAllProjects_ForClient_ReturnsOnlyClientProjects() {
        User clientUser = User.builder().id(99L).role(UserRole.CLIENT).build();
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(clientUser));
        when(projectRepository.findByClientId(99L)).thenReturn(List.of(sampleProject));

        List<ProjectResponse> result = projectService.getAllProjects();

        assertThat(result).hasSize(1);
        verify(projectRepository).findByClientId(99L);
    }

    @Test
    @DisplayName("getProjectById - Tìm thấy dự án theo ID")
    void getProjectById_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        ProjectDetailResponse response = projectService.getProjectById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getProjectById - Khách hàng xem dự án của chính mình thành công")
    void getProjectById_ForClient_OwnProject_Success() {
        User clientUser = User.builder().id(99L).role(UserRole.CLIENT).build();
        sampleProject.setClient(clientUser);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(clientUser));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        ProjectDetailResponse response = projectService.getProjectById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getProjectById - Khách hàng xem dự án của người khác bị chặn AccessDeniedException")
    void getProjectById_ForClient_OtherClientProject_ThrowsAccessDeniedException() {
        User clientUser = User.builder().id(99L).role(UserRole.CLIENT).build();
        User otherClient = User.builder().id(100L).role(UserRole.CLIENT).build();
        sampleProject.setClient(otherClient);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(clientUser));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        assertThatThrownBy(() -> projectService.getProjectById(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Khách hàng chỉ có quyền xem chi tiết dự án của mình");
    }

    @Test
    @DisplayName("updateProject - Cập nhật dự án thành công")
    void updateProject_Success() {
        ProjectUpdateRequest request = ProjectUpdateRequest.builder()
                .name("Tên Mới")
                .description("Mô tả mới")
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(projectRepository.existsByNameIgnoreCaseAndIdNot("Tên Mới", 1L)).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(sampleProject);

        ProjectResponse response = projectService.updateProject(1L, request);

        assertThat(response).isNotNull();
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("deleteProject - Xóa dự án thành công khi không có task dở dang")
    void deleteProject_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(taskRepository.countByProjectIdAndStatus(1L, TaskStatus.IN_PROGRESS)).thenReturn(0L);

        projectService.deleteProject(1L);

        verify(projectRepository).delete(sampleProject);
    }

    @Test
    @DisplayName("deleteProject - Bị chặn khi vẫn còn công việc IN_PROGRESS dở dang")
    void deleteProject_WithInProgressTasks_ThrowsBadRequestException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(taskRepository.countByProjectIdAndStatus(1L, TaskStatus.IN_PROGRESS)).thenReturn(2L);

        assertThatThrownBy(() -> projectService.deleteProject(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("công việc đang thực hiện dở dang");
    }

    @Test
    @DisplayName("deleteProject - Bị chặn khi vẫn còn công việc REWORK đang sửa lỗi")
    void deleteProject_WithReworkTasks_ThrowsBadRequestException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(taskRepository.countByProjectIdAndStatus(1L, TaskStatus.IN_PROGRESS)).thenReturn(0L);
        when(taskRepository.countByProjectIdAndStatus(1L, TaskStatus.REWORK)).thenReturn(1L);

        assertThatThrownBy(() -> projectService.deleteProject(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("công việc đang thực hiện dở dang hoặc cần sửa lỗi");
    }
}
