package com.demo.staffing_management_backend.dto;

import jakarta.validation.constraints.NotBlank;

public final class SkillDtos {
    private  SkillDtos(){}

    public record SkillRequest(@NotBlank String name,
                               String category,
                               String description) {
    }

    public record SkillResponse(String id,
                                String name,
                                String category,
                                String description) {
    }

}
