package com.github.deniskoriavets.sportvision.mapper;

import com.github.deniskoriavets.sportvision.dto.response.ParentResponse;
import com.github.deniskoriavets.sportvision.entity.Parent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParentMapper {
    ParentResponse toResponse(Parent parent);
}
