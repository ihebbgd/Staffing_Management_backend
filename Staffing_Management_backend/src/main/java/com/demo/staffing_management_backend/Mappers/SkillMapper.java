package com.demo.staffing_management_backend.Mappers;

import com.demo.staffing_management_backend.dto.SkillDtos;
import com.demo.staffing_management_backend.model.Skill;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    SkillDtos.SkillResponse toResponse(Skill skill);
}
