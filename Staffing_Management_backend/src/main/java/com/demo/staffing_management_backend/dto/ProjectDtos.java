package com.demo.staffing_management_backend.dto;

import com.demo.staffing_management_backend.model.enums.ProjectStatus;

import java.time.LocalDate;
import java.util.List;

public final class ProjectDtos {
    private ProjectDtos() {}

    public record ProjectRequest(String name,
                                 String description,
                                 LocalDate startDate,
                                 LocalDate endDate,
                                 ProjectStatus status,
                                 List<String> requiredSkillIds) {
    }

    public record  ProjectResponse(String Id,
                                   String name,
                                   String description,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   ProjectStatus status,
                                   List<String> requiredSkillIds) {

    }
}
