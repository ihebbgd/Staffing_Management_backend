package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.dto.AllocationDtos;
import com.demo.staffing_management_backend.dto.EmployeeDtos;
import com.demo.staffing_management_backend.dto.EmployeeSkillDtos;
import com.demo.staffing_management_backend.dto.RecommendationDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.model.Certification;
import com.demo.staffing_management_backend.model.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final double W_SKILL_MATCH = 0.40;
    private static final double W_SKILL_LEVEL = 0.25;
    private static final double W_AVAILABILITY = 0.20;
    private static final double W_CERT = 0.10;
    private static final double W_EXPERIENCE = 0.05;

    private final ProjectService projectService;
    private final EmployeeService employeeService;
    private final EmployeeSkillService employeeSkillService;
    private final CertificationService certificationService;
    private final AllocationService allocationService;

    public List<RecommendationDtos.RecommendationResult> recommendForProject(String projectId, int topN) {
        Project project = projectService.findOrThrow(projectId);
        List<String> required = project.getRequiredSkillIds();
        if (required == null || required.isEmpty()) {
            throw new BadRequestException("Project has no required skills to match against");
        }
        int effectiveTopN = topN > 0 ? topN : 5;
        Set<String> requiredSkillIds = new HashSet<>(required);
        int requiredCount = requiredSkillIds.size();
        List<RecommendationDtos.RecommendationResult> results = new ArrayList<>();
        for (EmployeeDtos.EmployeeResponse employee : employeeService.getAll()) {
            if (!employee.active()) {
                continue;
            }
            results.add(scoreEmployee(employee, requiredSkillIds, requiredCount));
        }
        results.sort(Comparator.comparingDouble(RecommendationDtos.RecommendationResult::totalScore).reversed());
        return results.stream().limit(effectiveTopN).toList();
    }

    private RecommendationDtos.RecommendationResult scoreEmployee(EmployeeDtos.EmployeeResponse employee, Set<String> requiredSkillIds, int requiredCount) {
        List<EmployeeSkillDtos.EmployeeSkillResponse> employeeSkills = employeeSkillService.getByEmployee(employee.id());
        int matchedCount = 0;
        int proficiencySum = 0;
        for (EmployeeSkillDtos.EmployeeSkillResponse es : employeeSkills) {
            if (requiredSkillIds.contains(es.skillId())) {
                matchedCount++;
                proficiencySum += es.proficiencyLevel();
            }
        }
        double skillMatch = (double) matchedCount / requiredCount;

        double skillLevel = matchedCount > 0 ? ((double) proficiencySum / matchedCount) / 5.0 : 0.0;

        AllocationDtos.WorkloadResponse workload = allocationService.getWorkload(employee.id());
        double availability = clamp(1.0-(workload.utilizationPercent() / 100.0));

        Set<String> certifiedRequiredSkillIds = new HashSet<>();
        for (Certification cert : certificationService.getActiveCertificationsForEmployee(employee.id())) {
            if (cert.getSkillId() != null && requiredSkillIds.contains(cert.getSkillId())) {
                certifiedRequiredSkillIds.add(cert.getSkillId());
            }
        }
        double certScore = (double) certifiedRequiredSkillIds.size() / requiredCount;

        double experience = clamp(employee.yearsOfExperience() / 10.0);

        double skillMatchContribution = skillMatch * W_SKILL_MATCH;
        double skillLevelContribution = skillLevel * W_SKILL_LEVEL;
        double availabilityContribution = availability * W_AVAILABILITY;
        double certScoreContribution = certScore * W_CERT;
        double experienceContribution = experience * W_EXPERIENCE;
        double total = skillMatchContribution + skillLevelContribution + availabilityContribution + certScoreContribution + experienceContribution;
        RecommendationDtos.FactorBreakdown breakdown = new RecommendationDtos.FactorBreakdown(
                round4(skillMatch), round4(skillLevel), round4(availability),
                round4(certScore), round4(experience),
                round4(skillMatchContribution), round4(skillLevelContribution),
                round4(availabilityContribution), round4(certScoreContribution),
                round4(experienceContribution),
                matchedCount, requiredCount);
        return new RecommendationDtos.RecommendationResult(
                employee.id(),
                employee.firstName() + " " + employee.lastName(),
                employee.jobTitle(),
                round4(total),
                breakdown);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }


}
