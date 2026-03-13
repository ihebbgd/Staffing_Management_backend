package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectRepository extends MongoRepository<Project, String> {
}
