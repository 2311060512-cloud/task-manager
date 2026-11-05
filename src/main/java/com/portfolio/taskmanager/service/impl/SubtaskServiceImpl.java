package com.portfolio.taskmanager.service.impl;

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
import com.portfolio.taskmanager.service.SubtaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SubtaskServiceImpl implements SubtaskService {

    private final SubtaskRepository subtaskRepository;
    private final TaskRepository taskRepository;
    private final SubtaskMapper subtaskMapper;

    @Override
    public SubtaskResponse createSubtask(Long taskId, SubtaskCreateRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", taskId));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new BadRequestException("Không thể thêm công việc con vào công việc đã hoàn thành (DONE). Vui lòng chuyển trạng thái công việc sang Đang làm hoặc Cần sửa lại trước.");
        }

        Subtask subtask = Subtask.builder()
                .title(request.getTitle().trim())
                .completed(false)
                .task(task)
                .build();

        Subtask saved = subtaskRepository.save(subtask);
        return subtaskMapper.toResponse(saved);
    }

    @Override
    public SubtaskResponse toggleSubtask(Long subtaskId) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc con", "id", subtaskId));

        if (subtask.getTask() != null && subtask.getTask().getStatus() == TaskStatus.DONE) {
            throw new BadRequestException("Không thể thay đổi trạng thái công việc con khi công việc chính đã hoàn thành (DONE). Vui lòng chuyển trạng thái công việc sang Đang làm hoặc Cần sửa lại trước.");
        }

        subtask.setCompleted(!subtask.isCompleted());
        Subtask updated = subtaskRepository.save(subtask);
        return subtaskMapper.toResponse(updated);
    }

    @Override
    public SubtaskResponse updateSubtask(Long subtaskId, SubtaskUpdateRequest request) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc con", "id", subtaskId));

        if (subtask.getTask() != null && subtask.getTask().getStatus() == TaskStatus.DONE) {
            throw new BadRequestException("Không thể sửa công việc con khi công việc chính đã hoàn thành (DONE). Vui lòng chuyển trạng thái công việc sang Đang làm hoặc Cần sửa lại trước.");
        }

        subtask.setTitle(request.getTitle().trim());
        Subtask updated = subtaskRepository.save(subtask);
        return subtaskMapper.toResponse(updated);
    }

    @Override
    public void deleteSubtask(Long subtaskId) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc con", "id", subtaskId));

        if (subtask.getTask() != null && subtask.getTask().getStatus() == TaskStatus.DONE) {
            throw new BadRequestException("Không thể xóa công việc con khi công việc chính đã hoàn thành (DONE). Vui lòng chuyển trạng thái công việc sang Đang làm hoặc Cần sửa lại trước.");
        }

        subtaskRepository.delete(subtask);
    }
}

