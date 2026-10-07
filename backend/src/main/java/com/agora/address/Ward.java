package com.agora.address;

import jakarta.persistence.*;

import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table
public class Ward {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "province_id", nullable = false)
    private AddressServiceImpl.Province province;
}
