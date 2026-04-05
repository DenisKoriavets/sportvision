package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.ChildRequest;
import com.github.deniskoriavets.sportvision.dto.ChildResponse;
import java.util.List;
import java.util.UUID;

public interface ChildService {
    ChildResponse addChild(ChildRequest request);

    List<ChildResponse> getAllChildrenForCurrentUser();

    ChildResponse getChildById(UUID id);

    ChildResponse updateChild(UUID id, ChildRequest request);

    void deleteChild(UUID id);
}
