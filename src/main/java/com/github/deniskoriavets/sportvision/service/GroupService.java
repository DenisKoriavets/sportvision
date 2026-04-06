package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.GroupRequest;
import com.github.deniskoriavets.sportvision.dto.GroupResponse;
import com.github.deniskoriavets.sportvision.dto.GroupSearchCriteria;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupService {
    GroupResponse createGroup(GroupRequest groupRequest);

    Page<GroupResponse> getGroups(GroupSearchCriteria criteria, Pageable pageable);

    GroupResponse getGroupById(UUID id);

    GroupResponse updateGroup(UUID id, GroupRequest groupRequest);

    void deleteGroup(UUID id);
}
