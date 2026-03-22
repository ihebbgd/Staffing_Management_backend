package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.SkillMapper;
import com.demo.staffing_management_backend.dto.SkillDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Project;
import com.demo.staffing_management_backend.model.Skill;
import com.demo.staffing_management_backend.repository.EmployeeSkillRepository;
import com.demo.staffing_management_backend.repository.ProjectRepository;
import com.demo.staffing_management_backend.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {
    private final SkillRepository skillRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final ProjectRepository projectRepository;
    private final SkillMapper skillMapper;

    public SkillDtos.SkillResponse create(SkillDtos.SkillRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Skill name is required");
        }
        if (skillRepository.existsByName(request.name())) {
            throw new BadRequestException("Skill already exists : " + request.name());
        }
        Skill skill = Skill.builder()
                .name(request.name())
                .category(request.category())
                .description(request.description())
                .build();
        return skillMapper.toResponse(skillRepository.save(skill));
    }

    public Page<SkillDtos.SkillResponse> getAll(Pageable pageable) {
        return skillRepository.findAll(pageable).map(skillMapper::toResponse);
    }

    public SkillDtos.SkillResponse getById(String id) {
        return skillMapper.toResponse(findOrThrow(id));
    }

    public SkillDtos.SkillResponse update(String id, SkillDtos.SkillRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Skill name is required");
        }
        Skill skill = findOrThrow(id);
        skill.setName(request.name());
        skill.setCategory(request.category());
        skill.setDescription(request.description());
        return skillMapper.toResponse(skillRepository.save(skill));
    }

    public void delete(String id) {
        Skill skill = findOrThrow(id);
        employeeSkillRepository.deleteBySkillId(id);
        List<Project> referencingProjects = projectRepository.findByRequiredSkillIdsContaining(id);
        for (Project project : referencingProjects) {
            if (project.getRequiredSkillIds() != null && project.getRequiredSkillIds().remove(id)) {
                projectRepository.save(project);
            }
        }
        skillRepository.delete(skill);
    }

    public Skill findOrThrow(String id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id : " + id));
    }
}
