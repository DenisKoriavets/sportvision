package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.request.GroupRequest;
import com.github.deniskoriavets.sportvision.dto.response.GroupResponse;
import com.github.deniskoriavets.sportvision.dto.criteria.GroupSearchCriteria;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupService {
    GroupResponse createGroup(GroupRequest groupRequest);

    Page<GroupResponse> getGroups(GroupSearchCriteria criteria, Pageable pageable);

    GroupResponse getGroupById(UUID id);

    GroupResponse updateGroup(UUID id, GroupRequest groupRequest);

    void deleteGroup(UUID id);

    List<GroupResponse> getGroupsByCoachId(UUID coachId);
}
