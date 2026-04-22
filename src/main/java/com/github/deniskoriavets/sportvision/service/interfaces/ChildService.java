package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.response.AttendanceResponse;
import com.github.deniskoriavets.sportvision.dto.request.ChildRequest;
import com.github.deniskoriavets.sportvision.dto.response.ChildResponse;
import com.github.deniskoriavets.sportvision.dto.criteria.ChildSearchCriteria;
import com.github.deniskoriavets.sportvision.dto.response.SubscriptionResponse;
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

    List<AttendanceResponse> getChildAttendance(UUID childId);

    List<SubscriptionResponse> getChildSubscriptions(UUID childId);
}
