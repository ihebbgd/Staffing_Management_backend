package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.config.RecommendationProperties;
import com.demo.staffing_management_backend.dto.RecommendationDtos;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.model.EmployeeSkill;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationScoringTest {

    private final RecommendationProperties properties = new RecommendationProperties(
            new RecommendationProperties.Weights(0.40, 0.25, 0.20, 0.10, 0.05), 10.0);
    private final RecommendationService service =
            new RecommendationService(null, null, null, null, null, properties);

    private EmployeeSkill skill(String employeeId, String skillId, int level) {
        return EmployeeSkill.builder().employeeId(employeeId).skillId(skillId).proficiencyLevel(level).build();
    }

    @Test
    void employeeWithNoMatchingSkills_isGatedToZero() {
        Employee e = Employee.builder().id("e1").firstName("No").lastName("Match")
                .weeklyCapacityHours(40).yearsOfExperience(10).build();

        RecommendationDtos.RecommendationResult result = service.scoreEmployee(
                e, Set.of("java", "spring", "mongo"), 3,
                List.of(skill("e1", "react", 5)), List.of(), 0.0);

        assertEquals(0.0, result.totalScore());
        assertEquals(0, result.breakdown().matchedSkillCount());
    }

    @Test
    void fullyQualifiedIdleSenior_scoresHigh() {
        Employee alice = Employee.builder().id("a").firstName("Alice").lastName("Martin")
                .jobTitle("Senior").weeklyCapacityHours(40).yearsOfExperience(8).build();

        RecommendationDtos.RecommendationResult result = service.scoreEmployee(
                alice, Set.of("java", "spring", "mongo"), 3,
                List.of(skill("a", "java", 5), skill("a", "spring", 5), skill("a", "mongo", 5)),
                List.of(), 0.0);

        // skillMatch=1.0, skillLevel=1.0, availability=1.0 -> already 0.85 before experience.
        assertTrue(result.totalScore() > 0.85, "expected a strong score but was " + result.totalScore());
        assertEquals(3, result.breakdown().matchedSkillCount());
        assertEquals(1.0, result.breakdown().skillMatch());
    }

    @Test
    void partialMatch_scalesWithCoverage() {
        Employee bob = Employee.builder().id("b").firstName("Bob").lastName("Chen")
                .weeklyCapacityHours(40).yearsOfExperience(4).build();

        RecommendationDtos.RecommendationResult result = service.scoreEmployee(
                bob, Set.of("java", "spring", "mongo"), 3,
                List.of(skill("b", "java", 3)), List.of(), 0.0);

        // 1 of 3 skills matched.
        assertEquals(1, result.breakdown().matchedSkillCount());
        assertTrue(result.breakdown().skillMatch() < 0.34);
    }
}
