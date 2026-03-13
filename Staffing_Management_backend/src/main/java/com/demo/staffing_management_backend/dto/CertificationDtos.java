package com.demo.staffing_management_backend.dto;

import com.demo.staffing_management_backend.model.enums.CertificationStatus;

import java.time.LocalDate;

public final class CertificationDtos {
    private CertificationDtos() {}

    public record CertificationRequest(String employeeId,
                                       String name,
                                       String issuingOrganization,
                                       String skillId,
                                       LocalDate issueDate,
                                       LocalDate expiryDate,
                                       String credentialId) {
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
