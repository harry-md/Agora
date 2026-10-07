package com.agora.manufacturer;

import com.agora.address.Country;

import jakarta.persistence.*;

import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "manufacturer")
public class Manufacturer {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 150)
    private String slug;

    @Column(name = "logo_url", nullable = false, length = 500)
    private String logoURL;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
