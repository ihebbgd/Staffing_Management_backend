package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.Certification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CertificationRepostiory extends MongoRepository<Certification, String> {
    List<Certification> findByEmployeeId(String employeeId);
}
