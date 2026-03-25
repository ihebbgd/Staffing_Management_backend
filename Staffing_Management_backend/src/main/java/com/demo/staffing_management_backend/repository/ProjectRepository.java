package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.Project;
import com.demo.staffing_management_backend.model.enums.ProjectStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjectRepository extends MongoRepository<Project, String> {
    List<Project> findByRequiredSkillIdsContaining(String skillId);
    long countByStatus(ProjectStatus status);
}
