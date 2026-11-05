package com.portfolio.taskmanager.service;

import com.portfolio.taskmanager.dto.request.SubtaskCreateRequest;
import com.portfolio.taskmanager.dto.request.SubtaskUpdateRequest;
import com.portfolio.taskmanager.dto.response.SubtaskResponse;
import com.portfolio.taskmanager.entity.Subtask;
import com.portfolio.taskmanager.entity.Task;
import com.portfolio.taskmanager.entity.enums.TaskStatus;
import com.portfolio.taskmanager.exception.BadRequestException;
import com.portfolio.taskmanager.exception.ResourceNotFoundException;
import com.portfolio.taskmanager.mapper.SubtaskMapper;
import com.portfolio.taskmanager.repository.SubtaskRepository;
import com.portfolio.taskmanager.repository.TaskRepository;
import com.portfolio.taskmanager.service.impl.SubtaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubtaskServiceTest {

    @Mock
    private SubtaskRepository subtaskRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SubtaskMapper subtaskMapper;

    @InjectMocks
    private SubtaskServiceImpl subtaskService;

    private Task sampleTask;
    private Subtask sampleSubtask;

    @BeforeEach
    void setUp() {
        sampleTask = Task.builder()
                .id(1L)
                .title("Nhiệm vụ chính")
                .build();

        sampleSubtask = Subtask.builder()
                .id(10L)
                .title("Công việc con 1")
                .completed(false)
                .task(sampleTask)
                .build();
    }

    @Test
    @DisplayName("createSubtask - Tạo công việc con thành công")
    void createSubtask_Success() {
        SubtaskCreateRequest request = SubtaskCreateRequest.builder()
                .title("Viết API test")
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(subtaskRepository.save(any(Subtask.class))).thenReturn(sampleSubtask);
        when(subtaskMapper.toResponse(sampleSubtask)).thenReturn(
                SubtaskResponse.builder().id(10L).title("Công việc con 1").completed(false).build()
        );

        SubtaskResponse response = subtaskService.createSubtask(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        verify(subtaskRepository).save(any(Subtask.class));
    }

    @Test
    @DisplayName("createSubtask - Task không tồn tại ném lỗi ResourceNotFoundException")
    void createSubtask_TaskNotFound_ThrowsException() {
        SubtaskCreateRequest request = SubtaskCreateRequest.builder().title("Viết test").build();
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subtaskService.createSubtask(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Công việc");
    }

    @Test
    @DisplayName("toggleSubtask - Đổi trạng thái từ false sang true thành công")
    void toggleSubtask_Success() {
        when(subtaskRepository.findById(10L)).thenReturn(Optional.of(sampleSubtask));
        when(subtaskRepository.save(sampleSubtask)).thenReturn(sampleSubtask);
        when(subtaskMapper.toResponse(sampleSubtask)).thenReturn(
                SubtaskResponse.builder().id(10L).title("Công việc con 1").completed(true).build()
        );

        SubtaskResponse response = subtaskService.toggleSubtask(10L);

        assertThat(response).isNotNull();
        assertThat(sampleSubtask.isCompleted()).isTrue();
        verify(subtaskRepository).save(sampleSubtask);
    }

    @Test
    @DisplayName("updateSubtask - Cập nhật tiêu đề thành công")
    void updateSubtask_Success() {
        SubtaskUpdateRequest request = SubtaskUpdateRequest.builder().title("Tiêu đề mới").build();

        when(subtaskRepository.findById(10L)).thenReturn(Optional.of(sampleSubtask));
        when(subtaskRepository.save(sampleSubtask)).thenReturn(sampleSubtask);
        when(subtaskMapper.toResponse(sampleSubtask)).thenReturn(
                SubtaskResponse.builder().id(10L).title("Tiêu đề mới").completed(false).build()
        );

        SubtaskResponse response = subtaskService.updateSubtask(10L, request);

        assertThat(response).isNotNull();
        assertThat(sampleSubtask.getTitle()).isEqualTo("Tiêu đề mới");
        verify(subtaskRepository).save(sampleSubtask);
    }

    @Test
    @DisplayName("deleteSubtask - Xóa thành công")
    void deleteSubtask_Success() {
        when(subtaskRepository.findById(10L)).thenReturn(Optional.of(sampleSubtask));

        subtaskService.deleteSubtask(10L);

        verify(subtaskRepository).delete(sampleSubtask);
    }

    @Test
    @DisplayName("toggleSubtask - Bị chặn khi Task chính đã DONE (ném BadRequestException)")
    void toggleSubtask_WhenTaskIsDone_ThrowsBadRequestException() {
        sampleTask.setStatus(TaskStatus.DONE);
        when(subtaskRepository.findById(10L)).thenReturn(Optional.of(sampleSubtask));

        assertThatThrownBy(() -> subtaskService.toggleSubtask(10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Không thể thay đổi trạng thái công việc con khi công việc chính đã hoàn thành (DONE)");
    }

    @Test
    @DisplayName("createSubtask - Bị chặn khi Task chính đã DONE (ném BadRequestException)")
    void createSubtask_WhenTaskIsDone_ThrowsBadRequestException() {
        sampleTask.setStatus(TaskStatus.DONE);
        SubtaskCreateRequest request = SubtaskCreateRequest.builder().title("Subtask thêm vào task đã xong").build();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        assertThatThrownBy(() -> subtaskService.createSubtask(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Không thể thêm công việc con vào công việc đã hoàn thành (DONE)");
    }
}

