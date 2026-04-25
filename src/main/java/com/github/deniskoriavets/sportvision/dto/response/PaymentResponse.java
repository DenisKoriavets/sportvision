package com.github.deniskoriavets.sportvision.dto.response;

public record PaymentResponse(
    String checkoutUrl,
    String sessionId
) {
}
