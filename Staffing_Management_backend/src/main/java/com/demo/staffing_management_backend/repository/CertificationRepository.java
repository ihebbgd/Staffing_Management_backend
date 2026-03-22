package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.Certification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface CertificationRepository extends MongoRepository<Certification, String> {
    List<Certification> findByEmployeeId(String employeeId);
    List<Certification> findByEmployeeIdIn(Collection<String> employeeIds);
    void deleteByEmployeeId(String employeeId);
}
