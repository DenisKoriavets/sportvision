package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.BaseIntegrationTest;
import com.github.deniskoriavets.sportvision.service.interfaces.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        truncateAll();
    }

    @Test
    @DisplayName("Webhook returns 400 when Stripe signature is invalid")
    void webhook_ReturnsBadRequest_WhenSignatureIsInvalid() throws Exception {
        String payload = "{\"type\": \"checkout.session.completed\", \"data\": {}}";

        mockMvc.perform(post("/api/v1/payments/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Stripe-Signature", "invalid_signature")
                .content(payload))
            .andExpect(status().isBadRequest());

        verify(subscriptionService, never()).completePayment(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("Webhook returns 400 when Stripe-Signature header is missing")
    void webhook_ReturnsBadRequest_WhenSignatureHeaderIsMissing() throws Exception {
        String payload = "{\"type\": \"checkout.session.completed\"}";

        mockMvc.perform(post("/api/v1/payments/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest());

        verify(subscriptionService, never()).completePayment(org.mockito.ArgumentMatchers.anyString());
    }
}