package com.demo.staffing_management_backend.dto;

public final class SkillDtos {
    private  SkillDtos(){}

    public record SkillRequest(String name,
                               String category,
                               String description) {
    }

    public record SkillResponse(String id,
                                String name,
                                String category,
                                String description) {
    }

}
