package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.request.SubscriptionPlanRequest;
import com.github.deniskoriavets.sportvision.dto.response.SubscriptionPlanResponse;
import com.github.deniskoriavets.sportvision.entity.Section;
import com.github.deniskoriavets.sportvision.entity.SubscriptionPlan;
import com.github.deniskoriavets.sportvision.mapper.SubscriptionPlanMapper;
import com.github.deniskoriavets.sportvision.repository.SectionRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionPlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanServiceImplTest {

    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock private SubscriptionPlanMapper subscriptionPlanMapper;
    @Mock private SectionRepository sectionRepository;

    @InjectMocks private SubscriptionPlanServiceImpl subscriptionPlanService;

    @Test
    @DisplayName("Creates subscription plan successfully")
    void createSubscriptionPlan_Success() {
        UUID sectionId = UUID.randomUUID();
        SubscriptionPlanRequest request = new SubscriptionPlanRequest(
            "Базовий", new BigDecimal("1000"), 8, sectionId, 30, false
        );

        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(new Section()));
        when(subscriptionPlanMapper.toEntity(request)).thenReturn(new SubscriptionPlan());
        when(subscriptionPlanRepository.save(any())).thenReturn(new SubscriptionPlan());
        when(subscriptionPlanMapper.toResponse(any())).thenReturn(mock(SubscriptionPlanResponse.class));

        assertNotNull(subscriptionPlanService.createSubscriptionPlan(request));
        verify(subscriptionPlanRepository).save(any());
    }
}