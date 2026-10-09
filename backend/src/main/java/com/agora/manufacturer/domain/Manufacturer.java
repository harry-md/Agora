package com.agora.manufacturer.domain;

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
@Table(name = "manufacturers")
class Manufacturer {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 150)
    private String slug;

    @Column(nullable = false, length = 500)
    private String logoURL;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private UUID countryId;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;
}
