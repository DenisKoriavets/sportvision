package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.SectionRequest;
import com.github.deniskoriavets.sportvision.dto.SectionResponse;
import com.github.deniskoriavets.sportvision.dto.SectionSearchCriteria;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SectionService {
    SectionResponse createSection(SectionRequest sectionRequest);

    Page<SectionResponse> getSections(SectionSearchCriteria criteria, Pageable pageable);

    SectionResponse getSectionById(UUID id);

    SectionResponse updateSection(UUID id, SectionRequest sectionRequest);

    void deleteSection(UUID id);
}
