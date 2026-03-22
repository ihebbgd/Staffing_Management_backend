package com.demo.staffing_management_backend.model;


import com.demo.staffing_management_backend.model.enums.AllocationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "allocations")
@CompoundIndex(name = "emp_status_idx", def = "{'employeeId': 1, 'status': 1}")
public class Allocation {
    @Id
    private String id;

    private String employeeId;

    private String projectId;

    private double allocatedHoursPerWeek;

    private LocalDate startDate;

    private LocalDate endDate;

    private AllocationStatus status;

    private String roleOnProject;

    @CreatedDate
    private Instant createdAt;
}
