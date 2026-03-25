package com.demo.staffing_management_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "employees")
public class Employee {

    @Id
    private String id;

    @Indexed(unique = true, partialFilter = "{ 'userId': { $type: 'string' } }")
    private String userId;

    private String firstName;

    private String lastName;

    @Indexed(unique = true)
    private String email;

    private String jobTitle;

    private String department;

    private double weeklyCapacityHours;

    private double yearsOfExperience;

    private boolean active;

    @CreatedDate
    private Instant createdAt;
}
