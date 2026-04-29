package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.request.TelegramLinkRequest;
import com.github.deniskoriavets.sportvision.dto.response.ParentResponse;
import com.github.deniskoriavets.sportvision.dto.request.ParentUpdateRequest;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.exception.ResourceNotFoundException;
import com.github.deniskoriavets.sportvision.mapper.ParentMapper;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import com.github.deniskoriavets.sportvision.service.interfaces.ParentService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    public void linkTelegram(TelegramLinkRequest request) {
        Parent parent = securityFacade.getCurrentUser();
        parent.setTelegramChatId(request.chatId());
        parentRepository.save(parent);
    }

    @Override
    public Page<ParentResponse> getAllParents(Pageable pageable) {
        return parentRepository.findAll(pageable)
            .map(parentMapper::toResponse);
    }

    @Override
    public void deactivateParent(UUID id) {
        Parent parent = parentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
        parent.setActive(false);
        parentRepository.save(parent);
    }

    @Override
    public Page<ParentResponse> getAllCoaches(Pageable pageable) {
        return parentRepository.findAllByRole(Role.COACH, pageable)
            .map(parentMapper::toResponse);
    }

    @Override
    public void assignCoachRole(UUID id) {
        Parent parent = parentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
        parent.setRole(Role.COACH);
        parentRepository.save(parent);
    }

    @Override
    public void revokeCoachRole(UUID id) {
        Parent parent = parentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));

        if (parent.getRole() != Role.COACH) {
            throw new IllegalStateException("Parent is not a coach, cannot revoke coach role");
        }

        parent.setRole(Role.PARENT);
        parentRepository.save(parent);
    }
}
