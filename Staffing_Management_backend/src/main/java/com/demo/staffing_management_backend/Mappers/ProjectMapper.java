package com.demo.staffing_management_backend.Mappers;

import com.demo.staffing_management_backend.dto.ProjectDtos;
import com.demo.staffing_management_backend.model.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectDtos.ProjectResponse toResponse(Project project);
}
