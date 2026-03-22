package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.EmployeeMapper;
import com.demo.staffing_management_backend.dto.EmployeeDtos;
import com.demo.staffing_management_backend.exception.DuplicateResourceException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.repository.AllocationRepository;
import com.demo.staffing_management_backend.repository.CertificationRepository;
import com.demo.staffing_management_backend.repository.EmployeeRepository;
import com.demo.staffing_management_backend.repository.EmployeeSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final CertificationRepository certificationRepository;
    private final AllocationRepository allocationRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeDtos.EmployeeResponse create(EmployeeDtos.EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists : " + request.email());
        }
        Employee employee = Employee.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .jobTitle(request.jobTitle())
                .department(request.department())
                .weeklyCapacityHours(request.weeklyCapacityHours())
                .yearsOfExperience(request.yearsOfExperience())
                .active(request.active() == null || request.active())
                .build();
        return employeeMapper.toResponse(employeeRepository.save(employee));
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

    public EmployeeDtos.EmployeeResponse update(String id, EmployeeDtos.EmployeeRequest request) {
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

    public void delete(String id) {
        Employee employee = findOrThrow(id);
        employeeSkillRepository.deleteByEmployeeId(id);
        certificationRepository.deleteByEmployeeId(id);
        allocationRepository.deleteByEmployeeId(id);
        employeeRepository.delete(employee);
    }

    public Employee findOrThrow(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id : " + id));
    }
}
