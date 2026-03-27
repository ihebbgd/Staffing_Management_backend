package com.demo.staffing_management_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public final class EmployeeSkillDtos {

    private EmployeeSkillDtos() {
    }

    public record EmployeeSkillRequest(@NotBlank String employeeId,
                                       @NotBlank String skillId,
                                       @Min(value = 1, message = "Proficiency level must be between 1 and 5")
                                       @Max(value = 5, message = "Proficiency level must be between 1 and 5")
                                       int proficiencyLevel) {
    }

    public record EmployeeSkillResponse(String id,
                                        String employeeId,
                                        String skillId,
                                        int proficiencyLevel) {
    }

}
