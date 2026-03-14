package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.EmployeeMapper;
import com.demo.staffing_management_backend.dto.EmployeeDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.DuplicateResourceException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    
    public EmployeeDtos.EmployeeResponse create(EmployeeDtos.EmployeeRequest request){
        validate(request);
        if(employeeRepository.existsByEmail(request.email())){
            throw new DuplicateResourceException("Email already exists : "+request.email());
        }
        Employee employee=Employee.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .jobTitle(request.jobTitle())
                .department(request.department())
                .weeklyCapacityHours(request.weeklyCapacityHours())
                .yearsOfExperience(request.yearsOfExperience())
                .active(request.active() ==null || request.active())
                .createdAt(Instant.now())
                .build();
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }
    public List<EmployeeDtos.EmployeeResponse> getAll(){
        return employeeRepository.findAll().stream()
                .map(employeeMapper::toResponse)
                .toList();
    }
    public EmployeeDtos.EmployeeResponse getById(String id){
        return employeeMapper.toResponse(findOrThrow(id));
    }

    public EmployeeDtos.EmployeeResponse update(String id, EmployeeDtos.EmployeeRequest request){
        validate(request);
        Employee employee=findOrThrow(id);
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setJobTitle(request.jobTitle());
        employee.setDepartment(request.department());
        employee.setWeeklyCapacityHours(request.weeklyCapacityHours());
        employee.setYearsOfExperience(request.yearsOfExperience());
        if (request.active() != null){
            employee.setActive(request.active());
        }
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }
    public void delete(String id){
        Employee employee=findOrThrow(id);
        employeeRepository.delete(employee);
    }

    public Employee findOrThrow(String id){
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id : " + id));
    }

    private void validate(EmployeeDtos.EmployeeRequest request) {
        if(request.firstName()== null || request.firstName().isBlank()){
            throw new BadRequestException("First name is required");
        }
        if(request.lastName()== null || request.lastName().isBlank()){
            throw new BadRequestException("Last name is required");
        }
        if(request.email()== null || request.email().isBlank()){
            throw new BadRequestException("Email is required");
        }
        if(request.weeklyCapacityHours()<=0){
            throw new BadRequestException("Weekly capacity hours must be greater than 0");
        }
        if(request.yearsOfExperience()<0){
            throw new BadRequestException("Years of experience cannot be negative");
        }
    }
}
