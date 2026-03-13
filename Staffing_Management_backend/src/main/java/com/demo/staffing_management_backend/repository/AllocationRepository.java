package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.Allocation;
import com.demo.staffing_management_backend.model.enums.AllocationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AllocationRepository extends MongoRepository<Allocation, String> {
    List<Allocation> findByEmployeeId(String employeeId);
    List<Allocation> findByEmployeeIdAndStatus(String employeeId, AllocationStatus status);

}
