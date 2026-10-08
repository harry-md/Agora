package com.agora.product.domain;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product_categories")
class ProductCategory {
    @EmbeddedId
    private ProductCategoryId id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
