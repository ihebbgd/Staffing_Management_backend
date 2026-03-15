package com.demo.staffing_management_backend.Mappers;

import com.demo.staffing_management_backend.dto.AllocationDtos;
import com.demo.staffing_management_backend.model.Allocation;
import com.demo.staffing_management_backend.service.EmployeeService;
import com.demo.staffing_management_backend.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AllocationMapper {
    private final WorkloadService workloadService;
    private final EmployeeService employeeService;


    public AllocationDtos.AllocationResponse toResponse(Allocation a) {
        double utilization = workloadService.utilizationPercent(employeeService.findOrThrow(a.getEmployeeId()));
        return toResponse(a,utilization > 100.0, utilization);
    }

    public AllocationDtos.AllocationResponse toResponse(Allocation a, boolean warning, double
            utilization) {
        return new AllocationDtos.AllocationResponse(
                a.getId(), a.getEmployeeId(), a.getProjectId(), a.getAllocatedHoursPerWeek(),
                a.getStartDate(), a.getEndDate(), a.getStatus(), a.getRoleOnProject(),
                a.getCreatedAt(), warning, utilization);
    }
}
