package com.demo.staffing_management_backend.Mappers;

import com.demo.staffing_management_backend.dto.CertificationDtos;
import com.demo.staffing_management_backend.model.Certification;
import com.demo.staffing_management_backend.model.enums.CertificationStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CertificationMapper {

    public static CertificationStatus computeStatus(LocalDate expiryDate) {
        if (expiryDate == null) {
            return CertificationStatus.ACTIVE;
        }
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) {
            return CertificationStatus.EXPIRED;
        }
        if (!expiryDate.isAfter(today.plusDays(30))) {
            return CertificationStatus.EXPIRING_SOON;
        }
        return CertificationStatus.ACTIVE;
    }

    public CertificationDtos.CertificationResponse toResponse(Certification c) {
        return new CertificationDtos.CertificationResponse(
                c.getId(), c.getEmployeeId(), c.getName(), c.getIssuingOrganization(),
                c.getSkillId(), c.getIssueDate(), c.getExpiryDate(),
                c.getStatus(), c.getCredentialId());
    }
}
