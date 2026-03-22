package com.demo.staffing_management_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public final class EmployeeSkillDtos {

    private EmployeeSkillDtos() {
    }

    public record EmployeeSkillRequest(@NotBlank String employeeId,
                                       @NotBlank String skillId,
                                       @Min(1) @Max(5) int proficiencyLevel) {
    }

    public record EmployeeSkillResponse(String id,
                                        String employeeId,
                                        String skillId,
                                        int proficiencyLevel) {
    }

}
