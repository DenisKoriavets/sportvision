package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.ParentResponse;
import com.github.deniskoriavets.sportvision.dto.ParentUpdateRequest;

public interface ParentService {
    ParentResponse getCurrentParent();
    ParentResponse updateCurrentParent(ParentUpdateRequest request);
}