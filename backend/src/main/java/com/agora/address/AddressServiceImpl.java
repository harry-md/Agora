package com.agora.address;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

public class AddressServiceImpl {
    @Entity
    @Table
    public static class Province {
        @Id
        @GeneratedValue
        @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
        @Column(nullable = false, updatable = false)
        private UUID id;

        @Column(nullable = false)
        private String name;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "country_id", nullable = false)
        private Country country;
    }
}
