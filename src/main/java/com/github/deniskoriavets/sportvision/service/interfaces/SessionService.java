package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.request.SessionGenerationRequest;
import com.github.deniskoriavets.sportvision.dto.request.SessionRequest;
import com.github.deniskoriavets.sportvision.dto.response.SessionResponse;
import com.github.deniskoriavets.sportvision.dto.criteria.SessionSearchCriteria;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SessionService {
    void generateSessions(SessionGenerationRequest request);
    
    SessionResponse createManualSession(SessionRequest request);
    
    void cancelSession(UUID sessionId, String reason);
    
    List<SessionResponse> getSessionsByGroup(UUID groupId, LocalDate start, LocalDate end);

    Page<SessionResponse> searchSessions(SessionSearchCriteria criteria, Pageable pageable);
}