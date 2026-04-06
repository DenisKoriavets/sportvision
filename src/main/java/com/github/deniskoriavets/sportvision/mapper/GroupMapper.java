package com.github.deniskoriavets.sportvision.mapper;

import com.github.deniskoriavets.sportvision.dto.GroupRequest;
import com.github.deniskoriavets.sportvision.dto.GroupResponse;
import com.github.deniskoriavets.sportvision.entity.Group;
import com.github.deniskoriavets.sportvision.entity.Parent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    @Mapping(source = "section.id", target = "sectionId")
    @Mapping(source = "section.name", target = "sectionName")
    @Mapping(target = "coachName", source = "coach")
    @Mapping(target = "currentOccupancy", ignore = true)
    GroupResponse toResponse(Group group);

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