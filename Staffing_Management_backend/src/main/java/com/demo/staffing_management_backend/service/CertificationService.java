package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.CertificationMapper;
import com.demo.staffing_management_backend.dto.CertificationDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Certification;
import com.demo.staffing_management_backend.model.enums.CertificationStatus;
import com.demo.staffing_management_backend.repository.CertificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.demo.staffing_management_backend.Mappers.CertificationMapper.computeStatus;

@Service
@RequiredArgsConstructor
public class CertificationService {
    private final CertificationRepository certificationRepository;
    private final EmployeeService employeeService;
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

    public List<CertificationDtos.CertificationResponse> getAll() {
        return certificationRepository.findAll().stream().map(certificationMapper::toResponse).toList();
    }

    public CertificationDtos.CertificationResponse getById(String id) {
        return certificationMapper.toResponse(findOrThrow(id));
    }

    public List<CertificationDtos.CertificationResponse> getByEmployee(String employeeId) {
        return certificationRepository.findByEmployeeId(employeeId).stream().map(certificationMapper::toResponse).toList();
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


    private Certification findOrThrow(String id) {
        return certificationRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Certification not found: " + id));
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
