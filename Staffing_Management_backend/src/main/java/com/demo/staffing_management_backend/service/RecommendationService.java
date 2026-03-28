package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.CertificationMapper;
import com.demo.staffing_management_backend.config.RecommendationProperties;
import com.demo.staffing_management_backend.dto.RecommendationDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.model.Certification;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.model.EmployeeSkill;
import com.demo.staffing_management_backend.model.Project;
import com.demo.staffing_management_backend.model.enums.CertificationStatus;
import com.demo.staffing_management_backend.repository.CertificationRepository;
import com.demo.staffing_management_backend.repository.EmployeeRepository;
import com.demo.staffing_management_backend.repository.EmployeeSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int MAX_TOP_N = 50;
    private static final int DEFAULT_TOP_N = 5;
    private static final double MAX_PROFICIENCY = 5.0;

    private final ProjectService projectService;
    private final EmployeeRepository employeeRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final CertificationRepository certificationRepository;
    private final WorkloadService workloadService;
    private final RecommendationProperties properties;

    public List<RecommendationDtos.RecommendationResult> recommendForProject(String projectId, int topN) {
        Project project = projectService.findOrThrow(projectId);
        List<String> required = project.getRequiredSkillIds();
        if (required == null || required.isEmpty()) {
            throw new BadRequestException("Project has no required skills to match against");
        }
        Set<String> requiredSkillIds = new HashSet<>(required);
        int requiredCount = requiredSkillIds.size();
        int effectiveTopN = topN > 0 ? Math.min(topN, MAX_TOP_N) : DEFAULT_TOP_N;

        List<Employee> activeEmployees = employeeRepository.findByActiveTrue();
        if (activeEmployees.isEmpty()) {
            return List.of();
        }
        Set<String> employeeIds = activeEmployees.stream().map(Employee::getId).collect(Collectors.toSet());

        // Batch-load every collaborator once (was a per-employee N+1 before).
        Map<String, List<EmployeeSkill>> skillsByEmployee = employeeSkillRepository.findByEmployeeIdIn(employeeIds)
                .stream().collect(Collectors.groupingBy(EmployeeSkill::getEmployeeId));
        Map<String, List<Certification>> certsByEmployee = certificationRepository.findByEmployeeIdIn(employeeIds)
                .stream().collect(Collectors.groupingBy(Certification::getEmployeeId));
        LocalDate today = LocalDate.now();
        Map<String, Double> activeHoursByEmployee = workloadService.activeHoursByEmployee(employeeIds, today);

        return activeEmployees.stream()
                .map(e -> scoreEmployee(e, requiredSkillIds, requiredCount,
                        skillsByEmployee.getOrDefault(e.getId(), List.of()),
                        certsByEmployee.getOrDefault(e.getId(), List.of()),
                        activeHoursByEmployee.getOrDefault(e.getId(), 0.0)))
                .sorted(Comparator.comparingDouble(RecommendationDtos.RecommendationResult::totalScore).reversed())
                .limit(effectiveTopN)
                .toList();
    }

    RecommendationDtos.RecommendationResult scoreEmployee(Employee employee, Set<String> requiredSkillIds,
                                                          int requiredCount, List<EmployeeSkill> employeeSkills,
                                                          List<Certification> certifications, double activeHours) {
        int matchedCount = 0;
        int proficiencySum = 0;
        for (EmployeeSkill es : employeeSkills) {
            if (requiredSkillIds.contains(es.getSkillId())) {
                matchedCount++;
                proficiencySum += es.getProficiencyLevel();
            }
        }

        // Gate: an employee who matches none of the required skills is not a candidate.
        if (matchedCount == 0) {
            return new RecommendationDtos.RecommendationResult(employee.getId(),
                    fullName(employee), employee.getJobTitle(), 0.0,
                    new RecommendationDtos.FactorBreakdown(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, requiredCount));
        }

        double skillMatch = (double) matchedCount / requiredCount;
        // Depth = proficiency of matched skills, weighted by coverage (missing skills count as 0).
        double skillLevel = proficiencySum / (MAX_PROFICIENCY * requiredCount);

        double capacity = employee.getWeeklyCapacityHours();
        double utilization = capacity > 0 ? activeHours / capacity * 100.0 : 0.0;
        double availability = clamp(1.0 - utilization / 100.0);

        Set<String> certifiedRequiredSkillIds = new HashSet<>();
        for (Certification cert : certifications) {
            if (cert.getSkillId() != null && requiredSkillIds.contains(cert.getSkillId())
                    && CertificationMapper.computeStatus(cert.getExpiryDate()) == CertificationStatus.ACTIVE) {
                certifiedRequiredSkillIds.add(cert.getSkillId());
            }
        }
        double certScore = (double) certifiedRequiredSkillIds.size() / requiredCount;

        double experience = clamp(Math.log1p(employee.getYearsOfExperience())
                / Math.log1p(properties.experienceCapYears()));

        RecommendationProperties.Weights w = properties.weights();
        double skillMatchContribution = skillMatch * w.skillMatch();
        double skillLevelContribution = skillLevel * w.skillLevel();
        double availabilityContribution = availability * w.availability();
        double certScoreContribution = certScore * w.cert();
        double experienceContribution = experience * w.experience();
        double total = skillMatchContribution + skillLevelContribution + availabilityContribution
                + certScoreContribution + experienceContribution;

        RecommendationDtos.FactorBreakdown breakdown = new RecommendationDtos.FactorBreakdown(
                round4(skillMatch), round4(skillLevel), round4(availability),
                round4(certScore), round4(experience),
                round4(skillMatchContribution), round4(skillLevelContribution),
                round4(availabilityContribution), round4(certScoreContribution),
                round4(experienceContribution),
                matchedCount, requiredCount);
        return new RecommendationDtos.RecommendationResult(
                employee.getId(), fullName(employee), employee.getJobTitle(), round4(total), breakdown);
    }

    private String fullName(Employee e) {
        return e.getFirstName() + " " + e.getLastName();
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
