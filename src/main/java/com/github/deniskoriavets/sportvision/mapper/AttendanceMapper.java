package com.github.deniskoriavets.sportvision.mapper;

import com.github.deniskoriavets.sportvision.dto.AttendanceResponse;
import com.github.deniskoriavets.sportvision.entity.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "sessionName", expression = "java(attendance.getSession().getGroup().getSection().getName() + \" - \" + attendance.getSession().getDate())")
    AttendanceResponse toResponse(Attendance attendance);
}