package com.github.deniskoriavets.sportvision.mapper;

import com.github.deniskoriavets.sportvision.dto.ChildRequest;
import com.github.deniskoriavets.sportvision.dto.ChildResponse;
import com.github.deniskoriavets.sportvision.entity.Child;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChildMapper {
    @Mapping(source = "group.id", target = "groupId")
    ChildResponse toResponse(Child child);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Child toEntity(ChildRequest request);
}
