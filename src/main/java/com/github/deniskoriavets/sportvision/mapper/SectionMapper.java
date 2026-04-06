package com.github.deniskoriavets.sportvision.mapper;

import com.github.deniskoriavets.sportvision.dto.SectionRequest;
import com.github.deniskoriavets.sportvision.dto.SectionResponse;
import com.github.deniskoriavets.sportvision.entity.Section;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SectionMapper {
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Section toEntity(SectionRequest sectionRequest);

    SectionResponse toResponse(Section section);
}
