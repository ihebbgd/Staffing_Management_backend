package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjectRepository extends MongoRepository<Project, String> {
    List<Project> findByRequiredSkillIdsContaining(String skillId);
}
