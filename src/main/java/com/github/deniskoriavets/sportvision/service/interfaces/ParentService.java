package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.response.ParentResponse;
import com.github.deniskoriavets.sportvision.dto.request.ParentUpdateRequest;

public interface ParentService {
    ParentResponse getCurrentParent();
    ParentResponse updateCurrentParent(ParentUpdateRequest request);
}