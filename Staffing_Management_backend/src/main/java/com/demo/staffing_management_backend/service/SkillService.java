package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.SkillMapper;
import com.demo.staffing_management_backend.dto.SkillDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Skill;
import com.demo.staffing_management_backend.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {
    private final SkillRepository skillRepository;
    private final SkillMapper SkillMapper;

    public SkillDtos.SkillResponse create(SkillDtos.SkillRequest request) {
        if(request.name() == null || request.name().isBlank()){
            throw new BadRequestException("Skill name is required");
        }
        if (skillRepository.existsByName(request.name())) {
            throw new BadRequestException("Skill already exists : "+request.name());
        }
        Skill skill=Skill.builder()
                .name(request.name())
                .category(request.category())
                .description(request.description())
                .build();
        return SkillMapper.toResponse(skillRepository.save(skill));
    }
    public List<SkillDtos.SkillResponse> getall() {
        return skillRepository.findAll().stream()
                .map(SkillMapper::toResponse)
                .toList();
    }
    public SkillDtos.SkillResponse getById(String id) {
        return  SkillMapper.toResponse(findOrThrow(id));
    }
    public SkillDtos.SkillResponse update(String id, SkillDtos.SkillRequest request) {
        Skill skill=findOrThrow(id);
        skill.setName(request.name());
        skill.setCategory(request.category());
        skill.setDescription(request.description());
        return SkillMapper.toResponse(skillRepository.save(skill));
    }
    public void delete(String id) {
        Skill skill=findOrThrow(id);
        skillRepository.delete(skill);
    }

    public Skill findOrThrow(String id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id : " + id));
    }
}
