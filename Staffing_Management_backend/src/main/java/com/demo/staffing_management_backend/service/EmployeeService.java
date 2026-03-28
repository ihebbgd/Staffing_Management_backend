package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.EmployeeMapper;
import com.demo.staffing_management_backend.dto.EmployeeDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.DuplicateResourceException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.model.User;
import com.demo.staffing_management_backend.model.enums.UserRole;
import com.demo.staffing_management_backend.repository.AllocationRepository;
import com.demo.staffing_management_backend.repository.CertificationRepository;
import com.demo.staffing_management_backend.repository.EmployeeRepository;
import com.demo.staffing_management_backend.repository.EmployeeSkillRepository;
import com.demo.staffing_management_backend.repository.UserRepository;
import com.demo.staffing_management_backend.security.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private static final int MIN_LOGIN_PASSWORD_LENGTH = 8;
    private static final int GENERATED_PASSWORD_LENGTH = 12;

    private final EmployeeRepository employeeRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final CertificationRepository certificationRepository;
    private final AllocationRepository allocationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final EmployeeMapper employeeMapper;

    /**
     * Employee-first onboarding: creating an employee always provisions the linked login.
     * Runs in a single MongoDB transaction, so if persisting the employee fails the login insert
     * is rolled back automatically — a User can never exist without its Employee.
     */
    @Transactional
    public EmployeeDtos.EmployeeCreationResponse create(EmployeeDtos.EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists : " + request.email());
        }
        String username = (request.username() != null && !request.username().isBlank())
                ? request.username().trim() : request.email();
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already taken " + username);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("A login already exists for email " + request.email());
        }

        boolean generated = request.password() == null || request.password().isBlank();
        String rawPassword = generated ? PasswordGenerator.generate(GENERATED_PASSWORD_LENGTH) : request.password();
        if (!generated && rawPassword.length() < MIN_LOGIN_PASSWORD_LENGTH) {
            throw new BadRequestException("Login password must be at least " + MIN_LOGIN_PASSWORD_LENGTH + " characters");
        }
        UserRole role = request.role() != null ? request.role() : UserRole.EMPLOYEE;

        User user = userRepository.save(User.builder()
                .username(username)
                .email(request.email())
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .enabled(true)
                .tokenVersion(0)
                .build());

        Employee employee = employeeRepository.save(Employee.builder()
                .userId(user.getId())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .jobTitle(request.jobTitle())
                .department(request.department())
                .weeklyCapacityHours(request.weeklyCapacityHours())
                .yearsOfExperience(request.yearsOfExperience())
                .active(request.active() == null || request.active())
                .build());
        auditService.record("CREATE_EMPLOYEE", "EMPLOYEE", employee.getId(), "username=" + username);
        return new EmployeeDtos.EmployeeCreationResponse(
                employeeMapper.toResponse(employee), username, generated ? rawPassword : null);
    }

    public Page<EmployeeDtos.EmployeeResponse> getAll(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(employeeMapper::toResponse);
    }

    public EmployeeDtos.EmployeeResponse getById(String id) {
        return employeeMapper.toResponse(findOrThrow(id));
    }

    public EmployeeDtos.EmployeeResponse getByUserId(String userId) {
        return employeeMapper.toResponse(employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No employee profile linked to the current user")));
    }

    public EmployeeDtos.EmployeeResponse update(String id, EmployeeDtos.EmployeeUpdateRequest request) {
        Employee employee = findOrThrow(id);
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setJobTitle(request.jobTitle());
        employee.setDepartment(request.department());
        employee.setWeeklyCapacityHours(request.weeklyCapacityHours());
        employee.setYearsOfExperience(request.yearsOfExperience());
        if (request.active() != null) {
            employee.setActive(request.active());
        }
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    /**
     * Deletes the employee, its dependents (skills, certifications, allocations), and the linked login.
     * Runs in a single MongoDB transaction so the whole graph is removed atomically — a failure part-way
     * through rolls everything back rather than leaving orphaned dependents or a ghost employee.
     */
    @Transactional
    public void delete(String id) {
        Employee employee = findOrThrow(id);
        employeeSkillRepository.deleteByEmployeeId(id);
        certificationRepository.deleteByEmployeeId(id);
        allocationRepository.deleteByEmployeeId(id);
        if (employee.getUserId() != null) {
            userRepository.deleteById(employee.getUserId());
        }
        employeeRepository.delete(employee);
        auditService.record("DELETE_EMPLOYEE", "EMPLOYEE", id, null);
    }

    public Employee findOrThrow(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id : " + id));
    }
}
