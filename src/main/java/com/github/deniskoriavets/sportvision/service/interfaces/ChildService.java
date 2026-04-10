package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.ChildRequest;
import com.github.deniskoriavets.sportvision.dto.ChildResponse;
import java.util.List;
import java.util.UUID;

public interface ChildService {
    ChildResponse createChild(ChildRequest request);

    List<ChildResponse> getAllChildrenForCurrentUser();

    ChildResponse getChildById(UUID id);

    ChildResponse updateChild(UUID id, ChildRequest request);

    void deleteChild(UUID id);
}
