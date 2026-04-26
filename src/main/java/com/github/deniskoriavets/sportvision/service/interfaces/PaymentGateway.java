package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.response.PaymentResponse;
import com.stripe.exception.StripeException;
import java.util.Map;

public interface PaymentGateway {
    public PaymentResponse createPaymentSession(Long amount, String currency, String productName, Map<String, String> metadata)
        throws StripeException;
}
