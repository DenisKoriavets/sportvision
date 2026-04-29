package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.request.PaymentRequest;
import com.github.deniskoriavets.sportvision.dto.request.SubscriptionRequest;
import com.github.deniskoriavets.sportvision.dto.response.PaymentDetailResponse;
import com.github.deniskoriavets.sportvision.dto.response.PaymentResponse;
import com.github.deniskoriavets.sportvision.dto.response.SubscriptionResponse;
import com.stripe.exception.StripeException;
import java.util.List;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;

public interface SubscriptionService {
    SubscriptionResponse buySubscriptionManual(SubscriptionRequest request);

    List<SubscriptionResponse> getChildSubscriptions(UUID childId);

    void cancelSubscription(UUID id);

    SubscriptionResponse getSubscriptionById(UUID id);

    PaymentResponse initiatePayment(PaymentRequest request) throws StripeException;

    void completePayment(String stripeSessionId);

    List<PaymentDetailResponse> getMyPayments();

    PaymentDetailResponse getPaymentById(UUID paymentId);

    List<SubscriptionResponse> getAllSubscriptionsAdmin(List<SubscriptionStatus> statuses);

    SubscriptionResponse activateSubscriptionAdmin(UUID id);
}
