package com.github.deniskoriavets.sportvision.dto.response;

import com.github.deniskoriavets.sportvision.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentDetailResponse(
    UUID id,
    UUID subscriptionId,
    BigDecimal amount,
    PaymentStatus status,
    String stripeSessionUrl,
    LocalDateTime createdAt
) {
}
