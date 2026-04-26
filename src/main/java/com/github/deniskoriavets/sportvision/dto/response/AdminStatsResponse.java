package com.github.deniskoriavets.sportvision.dto.response;

import java.math.BigDecimal;

public record AdminStatsResponse(
    long totalParents,
    long totalChildren,
    long activeSubscriptions,
    BigDecimal totalRevenue
) {}