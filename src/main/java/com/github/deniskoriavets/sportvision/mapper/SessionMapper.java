package com.github.deniskoriavets.sportvision.mapper;

import com.github.deniskoriavets.sportvision.dto.SessionRequest;
import com.github.deniskoriavets.sportvision.dto.SessionResponse;
import com.github.deniskoriavets.sportvision.entity.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SessionMapper {

    @Mapping(target = "groupId", source = "group.id")
    SessionResponse toResponse(Session session);

    @Mapping(target = "group", ignore = true)
    @Mapping(target = "schedule", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "cancelReason", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Session toEntity(SessionRequest request);
}