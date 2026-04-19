package com.github.deniskoriavets.sportvision.mapper;

import com.github.deniskoriavets.sportvision.dto.ScheduleRequest;
import com.github.deniskoriavets.sportvision.dto.ScheduleResponse;
import com.github.deniskoriavets.sportvision.entity.Schedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScheduleMapper {

    @Mapping(target = "groupId", source = "group.id")
    ScheduleResponse toResponse(Schedule schedule);

    @Mapping(target = "group", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Schedule toEntity(ScheduleRequest request);

    @Mapping(target = "group", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(ScheduleRequest request, @MappingTarget Schedule schedule);
}