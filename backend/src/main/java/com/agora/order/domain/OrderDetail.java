package com.agora.order.domain;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "order_details")
public class OrderDetail {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column
    private Instant deliveredAt;

    @Column
    private Integer warrantyDurationValue;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarrantyDurationUnit warrantyDurationUnit = WarrantyDurationUnit.MONTH;

    @Column(nullable = false)
    private UUID warrantyPolicyId;

    @Column
    private LocalDate warrantyStartsOn;

    @Column
    private LocalDate warrantyExpiresOn;
}
