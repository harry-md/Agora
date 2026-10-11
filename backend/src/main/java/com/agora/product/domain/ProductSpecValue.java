package com.agora.product.domain;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(
        name = "product_spec_values",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_product_spec_values_product_spec",
                    columnNames = {"product_id", "spec_definition_id"})
        })
public class ProductSpecValue {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "spec_definition_id", nullable = false)
    private SpecDefinition specDefinition;

    @Column(nullable = true)
    private String textValue;

    @Column(nullable = true)
    private BigDecimal numberValue;

    @Column(nullable = true)
    private Boolean booleanValue;
}
