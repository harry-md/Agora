package com.agora.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.*;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
class ProductCategoryId {
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;
}
