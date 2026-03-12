package com.demo.staffing_management_backend.model;


import com.demo.staffing_management_backend.model.enums.AllocationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "allocations")
public class Allocation {
    @Id
    private String id;

    private String employeeId;

    private String projectId;

    private double allocatedHoursperweek;

    private LocalDate startDate;

    private LocalDate endDate;

    private AllocationStatus status;

    private String roleOnProject;

    private Instant createdAt;
}
