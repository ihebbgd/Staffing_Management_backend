package com.demo.staffing_management_backend.Mappers;

import com.demo.staffing_management_backend.dto.EmployeeDtos;
import com.demo.staffing_management_backend.dto.EmployeeSkillDtos;
import com.demo.staffing_management_backend.model.EmployeeSkill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeSkillMapper {
    EmployeeSkillDtos.EmployeeSkillResponse toResponse(EmployeeSkill employeeSkill);

}
