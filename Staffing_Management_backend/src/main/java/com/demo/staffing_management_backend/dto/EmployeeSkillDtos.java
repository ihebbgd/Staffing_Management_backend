package com.demo.staffing_management_backend.dto;

public final class EmployeeSkillDtos {

    private EmployeeSkillDtos() {
    }

    public record EmployeeSkillRequest(String employeeId,
                                       String skillId,
                                       int proficiencyLevel) {
    }

    public record EmployeeSkillResponse(String id,
                                        String employeeId,
                                       String skillId,
                                       int proficiencyLevel) {
    }

}
