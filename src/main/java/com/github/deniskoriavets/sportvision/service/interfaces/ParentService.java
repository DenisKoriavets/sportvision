package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.request.TelegramLinkRequest;
import com.github.deniskoriavets.sportvision.dto.response.ParentResponse;
import com.github.deniskoriavets.sportvision.dto.request.ParentUpdateRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParentService {
    ParentResponse getCurrentParent();

    ParentResponse updateCurrentParent(ParentUpdateRequest request);

    void linkTelegram(TelegramLinkRequest request);

    Page<ParentResponse> getAllParents(Pageable pageable);

    void deactivateParent(UUID id);

    Page<ParentResponse> getAllCoaches(Pageable pageable);

    void assignCoachRole(UUID id);

    void revokeCoachRole(UUID id);
}