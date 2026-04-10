package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.SectionRequest;
import com.github.deniskoriavets.sportvision.dto.SectionResponse;
import com.github.deniskoriavets.sportvision.dto.SectionSearchCriteria;
import com.github.deniskoriavets.sportvision.entity.Section;
import com.github.deniskoriavets.sportvision.exception.ResourceNotFoundException;
import com.github.deniskoriavets.sportvision.mapper.SectionMapper;
import com.github.deniskoriavets.sportvision.repository.SectionRepository;
import com.github.deniskoriavets.sportvision.repository.specification.SectionSpecifications;
import com.github.deniskoriavets.sportvision.service.interfaces.SectionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;

    private final SectionMapper sectionMapper;

    @Override
    @Transactional
    public SectionResponse createSection(SectionRequest sectionRequest) {
        return sectionMapper.toResponse(sectionRepository.save(sectionMapper.toEntity(sectionRequest)));
    }

    @Override
    public Page<SectionResponse> getSections(SectionSearchCriteria criteria, Pageable pageable) {
        Specification<Section> spec = SectionSpecifications.build(criteria);
        Page<Section> sectionPage = sectionRepository.findAll(spec, pageable);
        return sectionPage.map(sectionMapper::toResponse);
    }

    @Override
    public SectionResponse getSectionById(UUID id) {
        return sectionRepository.findById(id)
            .map(sectionMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));
    }

    @Override
    @Transactional
    public SectionResponse updateSection(UUID id, SectionRequest sectionRequest) {
        if (!sectionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Section not found with id: " + id);
        }
        var section = sectionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));
        section.setName(sectionRequest.name());
        section.setDescription(sectionRequest.description());
        return sectionMapper.toResponse(sectionRepository.save(section));
    }

    @Override
    @Transactional
    public void deleteSection(UUID id) {
        if (!sectionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Section not found with id: " + id);
        }
        sectionRepository.deleteById(id);
    }
}
