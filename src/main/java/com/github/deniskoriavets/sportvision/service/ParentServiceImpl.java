package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.response.ParentResponse;
import com.github.deniskoriavets.sportvision.dto.request.ParentUpdateRequest;
import com.github.deniskoriavets.sportvision.mapper.ParentMapper;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import com.github.deniskoriavets.sportvision.service.interfaces.ParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final ParentRepository parentRepository;
    private final ParentMapper parentMapper;
    private final SecurityFacade securityFacade;

    @Override
    @Transactional(readOnly = true)
    public ParentResponse getCurrentParent() {
        return parentMapper.toResponse(securityFacade.getCurrentUser());
    }

    @Override
    @Transactional
    public ParentResponse updateCurrentParent(ParentUpdateRequest request) {
        var parent = securityFacade.getCurrentUser();

        parent.setFirstName(request.firstName());
        parent.setLastName(request.lastName());
        parent.setPhone(request.phone());

        parent.getNotificationPreferences().clear();
        parent.getNotificationPreferences().addAll(request.notificationPreferences());

        return parentMapper.toResponse(parentRepository.save(parent));
    }
}