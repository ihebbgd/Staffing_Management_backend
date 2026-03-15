package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.ProjectMapper;
import com.demo.staffing_management_backend.dto.ProjectDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Project;
import com.demo.staffing_management_backend.model.enums.ProjectStatus;
import com.demo.staffing_management_backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectDtos.ProjectResponse create(ProjectDtos.ProjectRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Project name is required");
        }
        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(request.status() != null ? request.status() : ProjectStatus.PLANNED)
                .requiredSkillIds(request.requiredSkillIds() != null
                        ? new ArrayList<>(request.requiredSkillIds())
                        : new ArrayList<>())
                .build();
        return projectMapper.toResponse(projectRepository.save(project));
    }
    public List<ProjectDtos.ProjectResponse> getAll() {
        return projectRepository.findAll().stream().map(projectMapper::toResponse).toList();
    }

    public ProjectDtos.ProjectResponse getById(String id) {
        return projectMapper.toResponse(findOrThrow(id));
    }

    public ProjectDtos.ProjectResponse update(String id, ProjectDtos.ProjectRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Project name is required");
        }
        Project project = findOrThrow(id);
        project.setName(request.name());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        if (request.status() != null) {
            project.setStatus(request.status());
        }
        project.setRequiredSkillIds(request.requiredSkillIds() != null
                ? new ArrayList<>(request.requiredSkillIds())
                : new ArrayList<>());
        return projectMapper.toResponse(projectRepository.save(project));
    }

    public void delete(String id) {
        Project project = findOrThrow(id);
        projectRepository.delete(project);
    }

    public Project findOrThrow(String id) {
        return projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project not found : " + id));
    }


}
