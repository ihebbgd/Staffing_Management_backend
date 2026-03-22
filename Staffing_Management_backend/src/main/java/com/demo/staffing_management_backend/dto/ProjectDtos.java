package com.demo.staffing_management_backend.dto;

import com.demo.staffing_management_backend.model.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public final class ProjectDtos {
    private ProjectDtos() {}

    public record ProjectRequest(@NotBlank String name,
                                 String description,
                                 LocalDate startDate,
                                 LocalDate endDate,
                                 ProjectStatus status,
                                 List<String> requiredSkillIds) {
    }

    public record  ProjectResponse(String id,
                                   String name,
                                   String description,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   ProjectStatus status,
                                   List<String> requiredSkillIds) {

    }
}
