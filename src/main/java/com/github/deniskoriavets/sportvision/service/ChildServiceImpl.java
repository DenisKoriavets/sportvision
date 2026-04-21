package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.response.AttendanceResponse;
import com.github.deniskoriavets.sportvision.dto.request.ChildRequest;
import com.github.deniskoriavets.sportvision.dto.response.ChildResponse;
import com.github.deniskoriavets.sportvision.dto.criteria.ChildSearchCriteria;
import com.github.deniskoriavets.sportvision.dto.response.SubscriptionResponse;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.exception.ResourceNotFoundException;
import com.github.deniskoriavets.sportvision.mapper.AttendanceMapper;
import com.github.deniskoriavets.sportvision.mapper.ChildMapper;
import com.github.deniskoriavets.sportvision.mapper.SubscriptionMapper;
import com.github.deniskoriavets.sportvision.repository.AttendanceRepository;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import com.github.deniskoriavets.sportvision.repository.specification.ChildSpecifications;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import com.github.deniskoriavets.sportvision.service.interfaces.ChildService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final AttendanceRepository attendanceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AttendanceMapper attendanceMapper;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    @Transactional
    public ChildResponse createChild(ChildRequest request) {
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

    @Override
    @Transactional(readOnly = true)
    public Page<ChildResponse> searchChildren(ChildSearchCriteria criteria, Pageable pageable) {
        var currentUser = securityFacade.getCurrentUser();

        ChildSearchCriteria finalCriteria = criteria;

        if (currentUser.getRole() == Role.PARENT) {
            finalCriteria = new ChildSearchCriteria(
                criteria.query(),
                criteria.groupId(),
                currentUser.getId()
            );
        }

        var spec = ChildSpecifications.build(finalCriteria);
        return childRepository.findAll(spec, pageable)
            .map(childMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getChildAttendance(UUID childId) {
        getChildIfOwner(childId);

        return attendanceRepository.findAllByChildId(childId).stream()
            .map(attendanceMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getChildSubscriptions(UUID childId) {
        getChildIfOwner(childId);

        return subscriptionRepository.findAllByChildId(childId).stream()
            .map(subscriptionMapper::toResponse)
            .toList();
    }
}
