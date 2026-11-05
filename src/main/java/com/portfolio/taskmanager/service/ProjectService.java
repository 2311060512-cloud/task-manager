package com.portfolio.taskmanager.service;

import com.portfolio.taskmanager.dto.request.ProjectCreateRequest;
import com.portfolio.taskmanager.dto.request.ProjectUpdateRequest;
import com.portfolio.taskmanager.dto.response.ProjectDetailResponse;
import com.portfolio.taskmanager.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {

    ProjectResponse createProject(ProjectCreateRequest request);

    List<ProjectResponse> getAllProjects();

    ProjectDetailResponse getProjectById(Long id);

    ProjectResponse updateProject(Long id, ProjectUpdateRequest request);

    void deleteProject(Long id);
}

