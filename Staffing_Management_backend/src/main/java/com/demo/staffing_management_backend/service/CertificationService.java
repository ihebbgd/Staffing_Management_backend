package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.CertificationMapper;
import com.demo.staffing_management_backend.dto.CertificationDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Certification;
import com.demo.staffing_management_backend.model.enums.CertificationStatus;
import com.demo.staffing_management_backend.repository.CertificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.demo.staffing_management_backend.Mappers.CertificationMapper.computeStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificationService {
    private final CertificationRepository certificationRepository;
    private final EmployeeService employeeService;
    private final NotificationService notificationService;
    private final CertificationMapper certificationMapper;

    public CertificationDtos.CertificationResponse create(CertificationDtos.CertificationRequest request) {
        validate(request);
        employeeService.findOrThrow(request.employeeId());
        Certification certification = Certification.builder()
                .employeeId(request.employeeId())
                .name(request.name())
                .issuingOrganization(request.issuingOrganization())
                .skillId(request.skillId())
                .issueDate(request.issueDate())
                .expiryDate(request.expiryDate())
                .status(computeStatus(request.expiryDate()))
                .credentialId(request.credentialId())
                .build();
        return certificationMapper.toResponse(certificationRepository.save(certification));
    }

    public Page<CertificationDtos.CertificationResponse> getAll(Pageable pageable) {
        return certificationRepository.findAll(pageable).map(certificationMapper::toResponse);
    }

    public CertificationDtos.CertificationResponse getById(String id) {
        return certificationMapper.toResponse(findOrThrow(id));
    }

    public List<CertificationDtos.CertificationResponse> getByEmployee(String employeeId) {
        return certificationRepository.findByEmployeeId(employeeId).stream()
                .map(certificationMapper::toResponse).toList();
    }

    public CertificationDtos.CertificationResponse update(String id, CertificationDtos.CertificationRequest request) {
        validate(request);
        Certification certification = findOrThrow(id);
        certification.setName(request.name());
        certification.setIssuingOrganization(request.issuingOrganization());
        certification.setSkillId(request.skillId());
        certification.setIssueDate(request.issueDate());
        certification.setExpiryDate(request.expiryDate());
        certification.setCredentialId(request.credentialId());
        certification.setStatus(computeStatus(request.expiryDate()));
        return certificationMapper.toResponse(certificationRepository.save(certification));
    }

    public void delete(String id) {
        Certification certification = findOrThrow(id);
        certificationRepository.delete(certification);
    }

    public List<Certification> getActiveCertificationsForEmployee(String employeeId) {
        return certificationRepository.findByEmployeeId(employeeId).stream()
                .filter(c -> computeStatus(c.getExpiryDate()) == CertificationStatus.ACTIVE).toList();
    }

    @Scheduled(cron = "${app.certifications.refresh-cron:0 0 2 * * *}")
    public void refreshStatuses() {
        int updated = 0;
        for (Certification c : certificationRepository.findAll()) {
            CertificationStatus fresh = computeStatus(c.getExpiryDate());
            if (fresh != c.getStatus()) {
                CertificationStatus previous = c.getStatus();
                c.setStatus(fresh);
                certificationRepository.save(c);
                updated++;
                if (fresh == CertificationStatus.EXPIRING_SOON && previous != CertificationStatus.EXPIRING_SOON) {
                    notificationService.createSystemNotification(
                            c.getEmployeeId(),
                            "Certification expiring soon",
                            "Certification '" + c.getName() + "' expires on " + c.getExpiryDate(),
                            "CERT_EXPIRY");
                }
            }
        }
        if (updated > 0) {
            log.info("Refreshed status on {} certification(s).", updated);
        }
    }

    private Certification findOrThrow(String id) {
        return certificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found: " + id));
    }

    private void validate(CertificationDtos.CertificationRequest request) {
        if (request.employeeId() == null || request.employeeId().isBlank()) {
            throw new BadRequestException("employeeId is required");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Certification name is required");
        }
    }
}
