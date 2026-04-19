package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.ChildRequest;
import com.github.deniskoriavets.sportvision.dto.ChildResponse;
import com.github.deniskoriavets.sportvision.dto.ChildSearchCriteria;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChildService {
    ChildResponse createChild(ChildRequest request);

    List<ChildResponse> getAllChildrenForCurrentUser();

    ChildResponse getChildById(UUID id);

    ChildResponse updateChild(UUID id, ChildRequest request);

    void deleteChild(UUID id);

    Page<ChildResponse> searchChildren(ChildSearchCriteria criteria, Pageable pageable);
}
