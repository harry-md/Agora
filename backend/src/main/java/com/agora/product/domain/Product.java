package com.agora.product.domain;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "products")
class Product {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 150)
    private String slug;

    @Column(nullable = false)
    private UUID manufacturer;

    @Column(length = 500)
    private String thumbnail;

    @Builder.Default
    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private int ratingCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int soldCount = 0;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
}
