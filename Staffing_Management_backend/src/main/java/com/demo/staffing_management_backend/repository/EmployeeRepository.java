package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends MongoRepository<Employee, String> {
    boolean existsByEmail(String email);
    List<Employee> findByActiveTrue();
    Optional<Employee> findByUserId(String userId);
}
