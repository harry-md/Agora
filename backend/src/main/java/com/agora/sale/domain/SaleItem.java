package com.agora.sale.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sale_item")
public class SaleItem {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Min(0)
    @Column(nullable = false)
    private int quantity;

    @Min(0)
    @Column(nullable = false)
    private int soldQuantity;

    @Min(0)
    @Column(nullable = false)
    private int maxPerUser;
}
