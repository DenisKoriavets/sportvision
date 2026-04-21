package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.request.EnrollmentRequest;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.exception.ResourceNotFoundException;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.GroupRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import com.github.deniskoriavets.sportvision.service.interfaces.EnrollmentService;
import java.time.LocalDate;
import java.time.Period;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final GroupRepository groupRepository;
    private final ChildRepository childRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SecurityFacade securityFacade;

    @Override
    public void enrollChild(EnrollmentRequest enrollmentRequest) {
        var child = getChildIfOwner(enrollmentRequest.childId());
        var group = groupRepository.findById(enrollmentRequest.groupId())
            .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        int childAge = Period.between(child.getBirthDate(), LocalDate.now()).getYears();

        boolean isTooYoung = group.getAgeMin() != null && childAge < group.getAgeMin();
        boolean isTooOld = group.getAgeMax() != null && childAge > group.getAgeMax();

        if (isTooYoung || isTooOld) {
            throw new IllegalStateException(
                "Child does not meet the age requirements for this group");
        }
        int childrenInGroupAmount = childRepository.countByGroupId(group.getId());
        if (childrenInGroupAmount >= group.getMaxCapacity()) {
            throw new IllegalStateException(
                "Group is at full capacity");
        }
        if (subscriptionRepository.existsByChildIdAndSubscriptionPlanSectionIdAndStatus(
            child.getId(), group.getSection().getId(), SubscriptionStatus.ACTIVE)) {
            child.setGroup(group);
            childRepository.save(child);
        } else {
            throw new IllegalStateException(
                "Child does not have an active subscription for this section");
        }
    }

    @Override
    public void unenrollChild(EnrollmentRequest enrollmentRequest) {
        var child = getChildIfOwner(enrollmentRequest.childId());
        child.setGroup(null);
        childRepository.save(child);
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
