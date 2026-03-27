package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.EmployeeSkillMapper;
import com.demo.staffing_management_backend.dto.EmployeeSkillDtos;
import com.demo.staffing_management_backend.exception.DuplicateResourceException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.EmployeeSkill;
import com.demo.staffing_management_backend.repository.EmployeeSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeSkillService {
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeService employeeService;
    private final SkillService skillService;
    private final EmployeeSkillMapper employeeSkillMapper;

    public EmployeeSkillDtos.EmployeeSkillResponse create(EmployeeSkillDtos.EmployeeSkillRequest request) {
        employeeService.findOrThrow(request.employeeId());
        skillService.findOrThrow(request.skillId());
        if(employeeSkillRepository.existsByEmployeeIdAndSkillId(request.employeeId(), request.skillId())) {
            throw new DuplicateResourceException("This employee already has that skill assigned");
        }
        EmployeeSkill es=EmployeeSkill.builder()
                .employeeId(request.employeeId())
                .skillId(request.skillId())
                .proficiencyLevel(request.proficiencyLevel())
                .build();
        return employeeSkillMapper.toResponse(employeeSkillRepository.save(es));
    }

    public List<EmployeeSkillDtos.EmployeeSkillResponse> getAll() {
        return employeeSkillRepository.findAll().stream().map(employeeSkillMapper::toResponse).toList();
    }

    public EmployeeSkillDtos.EmployeeSkillResponse getById(String id) {
        return employeeSkillMapper.toResponse(findOrThrow(id));
    }

    public List<EmployeeSkillDtos.EmployeeSkillResponse> getByEmployee(String employeeId) {
        return employeeSkillRepository.findByEmployeeId(employeeId).stream().map(employeeSkillMapper::toResponse).toList();
    }

    public EmployeeSkillDtos.EmployeeSkillResponse update(String id, EmployeeSkillDtos.EmployeeSkillRequest request) {
        EmployeeSkill es=findOrThrow(id);
        es.setProficiencyLevel(request.proficiencyLevel());
        return employeeSkillMapper.toResponse(employeeSkillRepository.save(es));
    }

    public void delete(String id) {
        EmployeeSkill es=findOrThrow(id);
        employeeSkillRepository.delete(es);
    }


    private EmployeeSkill findOrThrow(String id) {
        return employeeSkillRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("EmployeeSkill not found: " + id));
    }
}
