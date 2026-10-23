package com.agora.product;

import java.math.BigDecimal;

public record ProductSummaryResponse(
        String name,
        String slug,
        String thumbnail,
        BigDecimal averageRating,
        int ratingCount,
        int soldCount) {}
