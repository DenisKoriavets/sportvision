package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.ChildRequest;
import com.github.deniskoriavets.sportvision.dto.ChildResponse;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.exception.ResourceNotFoundException;
import com.github.deniskoriavets.sportvision.mapper.ChildMapper;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import java.nio.channels.AcceptPendingException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChildServiceImpl implements ChildService {
    private final ChildRepository childRepository;
    private final ChildMapper childMapper;
    private final SecurityFacade securityFacade;

    @Override
    @Transactional
    public ChildResponse addChild(ChildRequest request) {
        var currentUser = securityFacade.getCurrentUser();
        var child = childMapper.toEntity(request);
        child.setParent(currentUser);
        return childMapper.toResponse(childRepository.save(child));
    }

    @Override
    public List<ChildResponse> getAllChildrenForCurrentUser() {
        return childRepository.findAllByParentId(securityFacade.getCurrentUser().getId()).stream()
            .map(childMapper::toResponse)
            .toList();
    }

    @Override
    public ChildResponse getChildById(UUID id) {
        return childMapper.toResponse(getChildIfOwner(id));
    }

    @Override
    @Transactional
    public ChildResponse updateChild(UUID id, ChildRequest request) {
        Child child = getChildIfOwner(id);
        child.setFirstName(request.firstName());
        child.setLastName(request.lastName());
        child.setBirthDate(request.birthDate());
        return childMapper.toResponse(childRepository.save(child));
    }

    @Override
    @Transactional
    public void deleteChild(UUID id) {
        childRepository.delete(getChildIfOwner(id));
    }

    private Child getChildIfOwner(UUID id) {
        Child child = childRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        if (!child.getParent().getId().equals(securityFacade.getCurrentUserId())) {
            throw new AccessDeniedException("Ви не маєте доступу до даних цієї дитини");
        }
        return child;
    }
}
