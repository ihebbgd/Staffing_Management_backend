package com.demo.staffing_management_backend.model;

import com.demo.staffing_management_backend.model.enums.CertificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "certifications")
public class Certification {
    @Id
    private String id;

    private String employeeId;

    private String name;

    private String issuingOrganization;

    /** option lien lel skill */
    private String skillId;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private CertificationStatus status;

    private String credentialId;
}
