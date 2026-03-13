package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.EmployeeSkill;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EmployeeSkillRepository extends MongoRepository<EmployeeSkill, String> {
    List<EmployeeSkill> findByEmployeeId(String employeeId);
    boolean existsByEmployeeIdAndSkillId(String employeeId, String skillId);
}
