package com.github.deniskoriavets.sportvision.mapper;

import com.github.deniskoriavets.sportvision.dto.request.GroupRequest;
import com.github.deniskoriavets.sportvision.dto.response.GroupResponse;
import com.github.deniskoriavets.sportvision.entity.Group;
import com.github.deniskoriavets.sportvision.entity.Parent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    @Mapping(target = "id", source = "group.id")
    @Mapping(target = "name", source = "group.name")
    @Mapping(target = "maxCapacity", source = "group.maxCapacity")
    @Mapping(target = "ageMin", source = "group.ageMin")
    @Mapping(target = "ageMax", source = "group.ageMax")
    @Mapping(target = "sectionId", source = "group.section.id")
    @Mapping(target = "sectionName", source = "group.section.name")
    @Mapping(target = "coachName", source = "group.coach")
    @Mapping(target = "currentOccupancy", source = "group.currentOccupancy")
    GroupResponse toResponse(Group group);

    @Mapping(target = "id", source = "group.id")
    @Mapping(target = "coachName", source = "group.coach")
    @Mapping(target = "currentOccupancy", source = "occupancy")
    @Mapping(target = "sectionId", source = "group.section.id")
    @Mapping(target = "sectionName", source = "group.section.name")
    GroupResponse toResponseWithOccupancy(Group group, Integer occupancy);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "coach", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "currentOccupancy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Group toEntity(GroupRequest request);

    default String mapCoachName(Parent coach) {
        if (coach == null) return "No coach assigned";
        return coach.getFirstName() + " " + coach.getLastName();
    }
}