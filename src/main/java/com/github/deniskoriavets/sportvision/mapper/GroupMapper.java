package com.github.deniskoriavets.sportvision.mapper;

import com.github.deniskoriavets.sportvision.dto.GroupRequest;
import com.github.deniskoriavets.sportvision.dto.GroupResponse;
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
    @Mapping(target = "currentOccupancy", source = "currentOccupancy")
    GroupResponse toResponse(Group group, Integer currentOccupancy);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "coach", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Group toEntity(GroupRequest request);

    default String mapCoachName(Parent coach) {
        if (coach == null) return "Тренер не призначений";
        return coach.getFirstName() + " " + coach.getLastName();
    }
}