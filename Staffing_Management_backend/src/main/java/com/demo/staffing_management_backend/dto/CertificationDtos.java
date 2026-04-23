package com.demo.staffing_management_backend.dto;

import com.demo.staffing_management_backend.model.enums.CertificationStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public final class CertificationDtos {
    private CertificationDtos() {}

    public record CertificationRequest(@NotBlank String employeeId,
                                       @NotBlank String name,
                                       String issuingOrganization,
                                       String skillId,
                                       LocalDate issueDate,
                                       LocalDate expiryDate,
                                       String credentialId) {

        @AssertTrue(message = "expiryDate must be on or after issueDate")
        public boolean isDateRangeValid() {
            return issueDate == null || expiryDate == null || !expiryDate.isBefore(issueDate);
        }
    }

    public record CertificationResponse(String id,
                                        String employeeId,
                                        String name,
                                        String issuingOrganization,
                                        String skillId,
                                        LocalDate issueDate,
                                        LocalDate expiryDate,
                                        CertificationStatus status,
                                        String credentialId){

    }
}
