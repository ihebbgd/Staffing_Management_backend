package com.demo.staffing_management_backend.config;

import com.demo.staffing_management_backend.model.*;
import com.demo.staffing_management_backend.model.enums.*;
import com.demo.staffing_management_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@Profile("!prod")
@ConditionalOnProperty(name = "app.seed.sample-data", havingValue = "true")
@RequiredArgsConstructor
public class SampleDataSeeder implements CommandLineRunner {

    private final SkillRepository skillRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final ProjectRepository projectRepository;
    private final AllocationRepository allocationRepository;
    private final CertificationRepository certificationRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.manager-password:Manager123!}")
    private String managerPassword;

    @Value("${app.seed.employee-password:Employee123!}")
    private String employeePassword;

    @Override
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            log.info("Sample data already present, skipping seed.");
            return;
        }
        log.info("Seeding sample data...");

        // ---------- Skills ----------
        Skill java   = skillRepository.save(Skill.builder().name("Java").category("Backend").description("Java programming").build());
        Skill spring = skillRepository.save(Skill.builder().name("Spring Boot").category("Backend").description("Spring Boot framework").build());
        Skill mongo  = skillRepository.save(Skill.builder().name("MongoDB").category("Database").description("MongoDB NoSQL database").build());
        Skill react  = skillRepository.save(Skill.builder().name("React").category("Frontend").description("React library").build());
        Skill docker = skillRepository.save(Skill.builder().name("Docker").category("DevOps").description("Containerization").build());

        // ---------- Employees ----------
        Employee alice = employeeRepository.save(Employee.builder()
                .firstName("Alice").lastName("Martin").email("alice.martin@staffing.local")
                .jobTitle("Senior Backend Engineer").department("Engineering")
                .weeklyCapacityHours(40).yearsOfExperience(8).active(true).createdAt(Instant.now()).build());
        Employee bob = employeeRepository.save(Employee.builder()
                .firstName("Bob").lastName("Chen").email("bob.chen@staffing.local")
                .jobTitle("Backend Engineer").department("Engineering")
                .weeklyCapacityHours(40).yearsOfExperience(4).active(true).createdAt(Instant.now()).build());
        Employee carla = employeeRepository.save(Employee.builder()
                .firstName("Carla").lastName("Diaz").email("carla.diaz@staffing.local")
                .jobTitle("Frontend Engineer").department("Engineering")
                .weeklyCapacityHours(35).yearsOfExperience(5).active(true).createdAt(Instant.now()).build());
        Employee david = employeeRepository.save(Employee.builder()
                .firstName("David").lastName("Okoye").email("david.okoye@staffing.local")
                .jobTitle("Junior Developer").department("Engineering")
                .weeklyCapacityHours(40).yearsOfExperience(1).active(true).createdAt(Instant.now()).build());
        Employee eva = employeeRepository.save(Employee.builder()
                .firstName("Eva").lastName("Novak").email("eva.novak@staffing.local")
                .jobTitle("DevOps Engineer").department("Operations")
                .weeklyCapacityHours(40).yearsOfExperience(6).active(true).createdAt(Instant.now()).build());

        // ---------- EmployeeSkills (proficiency 1..5) ----------
        saveSkill(alice, java, 5); saveSkill(alice, spring, 5); saveSkill(alice, mongo, 4);
        saveSkill(bob, java, 4);   saveSkill(bob, spring, 3);   saveSkill(bob, mongo, 2);
        saveSkill(carla, react, 5); saveSkill(carla, java, 2);
        saveSkill(david, java, 2);
        saveSkill(eva, docker, 5); saveSkill(eva, mongo, 3);

        // ---------- Projects ----------
        Project backend = projectRepository.save(Project.builder()
                .name("Backend API Platform").description("Core staffing REST API")
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusMonths(3))
                .status(ProjectStatus.ACTIVE)
                .requiredSkillIds(List.of(java.getId(), spring.getId(), mongo.getId())).build());
        Project portal = projectRepository.save(Project.builder()
                .name("Customer Portal UI").description("Customer-facing frontend")
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusMonths(2))
                .status(ProjectStatus.ACTIVE)
                .requiredSkillIds(List.of(react.getId(), java.getId())).build());
        Project infra = projectRepository.save(Project.builder()
                .name("Infra Migration").description("Move to containerized infra")
                .startDate(LocalDate.now().plusWeeks(2)).endDate(LocalDate.now().plusMonths(4))
                .status(ProjectStatus.PLANNED)
                .requiredSkillIds(List.of(docker.getId(), mongo.getId())).build());

        // ---------- Allocations ----------
        // Alice: 30 + 20 = 50h on a 40h capacity  -> OVER-ALLOCATED (125%)
        saveAllocation(alice, backend, 30, "Tech Lead");
        saveAllocation(alice, infra, 20, "Backend Support");
        // Carla: 20h on 35h capacity -> ~57%
        saveAllocation(carla, portal, 20, "Frontend Dev");
        // Eva: 15h on 40h capacity -> ~37%
        saveAllocation(eva, infra, 15, "DevOps");
        // Bob & David: no allocations -> fully available

        // ---------- Certifications (one of each status) ----------
        certificationRepository.save(Certification.builder()
                .employeeId(alice.getId()).name("Oracle Certified Professional: Java SE")
                .issuingOrganization("Oracle").skillId(java.getId())
                .issueDate(LocalDate.now().minusYears(2)).expiryDate(LocalDate.now().plusYears(1))
                .status(CertificationStatus.ACTIVE).credentialId("OCP-JAVA-1001").build());
        certificationRepository.save(Certification.builder()
                .employeeId(bob.getId()).name("Spring Certified Professional")
                .issuingOrganization("VMware").skillId(spring.getId())
                .issueDate(LocalDate.now().minusYears(1)).expiryDate(LocalDate.now().plusDays(15)) // within 30 days
                .status(CertificationStatus.EXPIRING_SOON).credentialId("SPRING-2002").build());
        certificationRepository.save(Certification.builder()
                .employeeId(eva.getId()).name("MongoDB Associate Developer")
                .issuingOrganization("MongoDB Inc.").skillId(mongo.getId())
                .issueDate(LocalDate.now().minusYears(2)).expiryDate(LocalDate.now().minusMonths(2)) // past
                .status(CertificationStatus.EXPIRED).credentialId("MONGO-3003").build());

        // ---------- Notifications ----------
        notificationRepository.save(Notification.builder().recipientId(null)
                .title("System ready").message("Sample data has been loaded.")
                .type("INFO").isRead(false).createdAt(Instant.now()).build());
        notificationRepository.save(Notification.builder().recipientId(alice.getId())
                .title("Over-allocation warning").message("You are allocated above 100% of your weekly capacity.")
                .type("ALLOCATION_CONFLICT").isRead(false).createdAt(Instant.now()).build());

        // ---------- Login users (so you can test each role) ----------
        userRepository.save(User.builder().username("manager").email("manager@staffing.local")
                .password(passwordEncoder.encode(managerPassword)).role(UserRole.MANAGER).enabled(true).createdAt(Instant.now()).build());
        User employeeUser = userRepository.save(User.builder().username("employee").email("employee@staffing.local")
                .password(passwordEncoder.encode(employeePassword)).role(UserRole.EMPLOYEE).enabled(true).createdAt(Instant.now()).build());

        // Link the demo 'employee' login to David so the personal dashboard (GET /api/me/*) works out of the box.
        david.setUserId(employeeUser.getId());
        employeeRepository.save(david);

        log.info("Sample data seeded. Backend project id = {}, Alice id = {}", backend.getId(), alice.getId());
    }

    private void saveSkill(Employee e, Skill s, int level) {
        employeeSkillRepository.save(EmployeeSkill.builder()
                .employeeId(e.getId()).skillId(s.getId()).proficiencyLevel(level).build());
    }

    private void saveAllocation(Employee e, Project p, double hours, String role) {
        allocationRepository.save(Allocation.builder()
                .employeeId(e.getId()).projectId(p.getId()).allocatedHoursPerWeek(hours)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusMonths(2))
                .status(AllocationStatus.ACTIVE).roleOnProject(role).createdAt(Instant.now()).build());
    }
}