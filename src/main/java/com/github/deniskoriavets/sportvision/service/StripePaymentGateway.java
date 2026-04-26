package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.response.PaymentResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.PaymentGateway;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripePaymentGateway implements PaymentGateway {

    @Value("${sportvision.stripe.success-url}")
    private String successUrl;

    @Value("${sportvision.stripe.cancel-url}")
    private String cancelUrl;

    @Override
    public PaymentResponse createPaymentSession(Long amount, String currency, String productName,
                                                Map<String, String> metadata) throws StripeException {
        var params = SessionCreateParams.builder()
            .setSuccessUrl(successUrl)
            .setCancelUrl(cancelUrl)
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .putAllMetadata(metadata)
            .addLineItem(SessionCreateParams.LineItem.builder()
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency(currency)
                    .setUnitAmount(amount)
                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(productName)
                        .build())
                    .build())
                .setQuantity(1L)
                .build())
            .build();

        var session = Session.create(params);
        return new PaymentResponse(session.getUrl(), session.getId());
    }
}