package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.EmployeeSkill;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface EmployeeSkillRepository extends MongoRepository<EmployeeSkill, String> {
    List<EmployeeSkill> findByEmployeeId(String employeeId);
    List<EmployeeSkill> findByEmployeeIdIn(Collection<String> employeeIds);
    boolean existsByEmployeeIdAndSkillId(String employeeId, String skillId);
    void deleteByEmployeeId(String employeeId);
    void deleteBySkillId(String skillId);
}
