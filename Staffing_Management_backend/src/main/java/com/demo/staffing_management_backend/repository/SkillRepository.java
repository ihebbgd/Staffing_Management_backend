package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.Skill;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SkillRepository extends MongoRepository<Skill, String> {
    boolean existsByName(String name);
}
