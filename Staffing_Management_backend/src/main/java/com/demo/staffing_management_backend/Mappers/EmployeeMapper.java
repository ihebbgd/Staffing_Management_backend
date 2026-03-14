package com.demo.staffing_management_backend.Mappers;

import com.demo.staffing_management_backend.dto.EmployeeDtos;
import com.demo.staffing_management_backend.model.Employee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeDtos.EmployeeResponse toResponse(Employee employee);
}
