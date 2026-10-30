package com.agora.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductDetailResponse(
        UUID id,
        String name,
        String slug,
        UUID manufacturerId,
        UUID categoryId,
        BigDecimal price,
        String thumbnail,
        BigDecimal averageRating,
        int ratingCount,
        int soldCount,
        Integer warrantyDurationValue,
        String warrantyDurationUnit,
        String description,
        List<String> images,
        List<ProductResponse.SpecValueResponse> specs) {}
