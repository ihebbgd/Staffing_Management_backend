package com.demo.staffing_management_backend.Mappers;

import com.demo.staffing_management_backend.dto.AllocationDtos;
import com.demo.staffing_management_backend.model.Allocation;
import org.springframework.stereotype.Component;

@Component
public class AllocationMapper {

    public AllocationDtos.AllocationResponse toResponse(Allocation a, boolean warning, double utilization) {
        return new AllocationDtos.AllocationResponse(
                a.getId(), a.getEmployeeId(), a.getProjectId(), a.getAllocatedHoursPerWeek(),
                a.getStartDate(), a.getEndDate(), a.getStatus(), a.getRoleOnProject(),
                a.getCreatedAt(), warning, utilization);
    }
}
