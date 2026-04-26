package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.response.AdminStatsResponse;
import com.github.deniskoriavets.sportvision.entity.enums.PaymentStatus;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.github.deniskoriavets.sportvision.repository.PaymentRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import com.github.deniskoriavets.sportvision.service.interfaces.AdminService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final ParentRepository parentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final ChildRepository childRepository;

    @Override
    public AdminStatsResponse getStats() {
        long totalParents = parentRepository.count();
        long totalChildren = childRepository.count();
        long activeSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        BigDecimal totalRevenue = paymentRepository.sumAmountByStatus(PaymentStatus.PAID);

        return new AdminStatsResponse(totalParents, totalChildren, activeSubscriptions, totalRevenue);
    }
}
