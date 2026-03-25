package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.Allocation;
import com.demo.staffing_management_backend.model.enums.AllocationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface AllocationRepository extends MongoRepository<Allocation, String> {
    List<Allocation> findByEmployeeId(String employeeId);
    List<Allocation> findByEmployeeIdAndStatus(String employeeId, AllocationStatus status);
    List<Allocation> findByStatus(AllocationStatus status);
    List<Allocation> findByEmployeeIdInAndStatus(Collection<String> employeeIds, AllocationStatus status);
    List<Allocation> findByProjectIdInAndStatus(Collection<String> projectIds, AllocationStatus status);
    void deleteByEmployeeId(String employeeId);
    void deleteByProjectId(String projectId);
}
