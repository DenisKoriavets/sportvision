package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.SessionGenerationRequest;
import com.github.deniskoriavets.sportvision.dto.SessionRequest;
import com.github.deniskoriavets.sportvision.dto.SessionResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SessionService {
    void generateSessions(SessionGenerationRequest request);
    
    SessionResponse createManualSession(SessionRequest request);
    
    void cancelSession(UUID sessionId, String reason);
    
    List<SessionResponse> getSessionsByGroup(UUID groupId, LocalDate start, LocalDate end);
}