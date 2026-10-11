package com.agora.product.domain;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(
        name = "spec_definitions",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_spec_definition_code", columnNames = "code")
        })
public class SpecDefinition {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private SpecDataType dataType;

    @Column(nullable = true)
    private String unit;
}
