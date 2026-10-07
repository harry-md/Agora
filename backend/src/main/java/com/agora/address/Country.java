package com.agora.address;

import jakarta.persistence.*;

import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Table
@Entity
public class Country {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;
}
