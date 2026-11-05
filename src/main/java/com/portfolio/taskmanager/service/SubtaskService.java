package com.portfolio.taskmanager.service;

import com.portfolio.taskmanager.dto.request.SubtaskCreateRequest;
import com.portfolio.taskmanager.dto.request.SubtaskUpdateRequest;
import com.portfolio.taskmanager.dto.response.SubtaskResponse;

public interface SubtaskService {

    SubtaskResponse createSubtask(Long taskId, SubtaskCreateRequest request);

    SubtaskResponse toggleSubtask(Long subtaskId);

    SubtaskResponse updateSubtask(Long subtaskId, SubtaskUpdateRequest request);

    void deleteSubtask(Long subtaskId);
}

