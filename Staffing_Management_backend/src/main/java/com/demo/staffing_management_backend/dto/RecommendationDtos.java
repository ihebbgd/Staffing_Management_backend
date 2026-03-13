package com.demo.staffing_management_backend.dto;

public class RecommendationDtos {
    private RecommendationDtos() {}

    public record FactorBreakdown(
            double skillMatch,
            double skillLevel,
            double availability,
            double certScore,
            double experience,
            double skillMatchContribution,
            double skillLevelContribution,
            double availabilityContribution,
            double certScoreContribution,
            double experienceContribution,
            int matchedSkillCount,
            int requiredSkillCount) {
    }

    public record RecommendationResult(
            String employeeId,
            String employeeName,
            String jobTitle,
            double totalScore,
            FactorBreakdown breakdown) {
    }
}
