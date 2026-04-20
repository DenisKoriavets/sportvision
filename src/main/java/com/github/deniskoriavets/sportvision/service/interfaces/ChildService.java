package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.AttendanceResponse;
import com.github.deniskoriavets.sportvision.dto.ChildRequest;
import com.github.deniskoriavets.sportvision.dto.ChildResponse;
import com.github.deniskoriavets.sportvision.dto.ChildSearchCriteria;
import com.github.deniskoriavets.sportvision.dto.SubscriptionResponse;
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

    public List<SubscriptionResponse> getChildSubscriptions(UUID childId);
}
