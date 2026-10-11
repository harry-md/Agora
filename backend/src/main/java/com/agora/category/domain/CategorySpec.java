package com.agora.category.domain;

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
        name = "category_specs",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_category_specs_category_spec_definition",
                    columnNames = {"category_id", "spec_definition_id"})
        })
public class CategorySpec {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private UUID specDefinitionId;

    @Builder.Default
    @Column(nullable = false)
    private boolean required = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean filterable = false;

    @Builder.Default
    @Column(nullable = false)
    private int displayOrder = 1;
}
