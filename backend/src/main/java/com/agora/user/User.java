package com.agora.user;

import com.agora.address.Address;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 200)
    private String username;

    @Column(nullable = false, length = 200)
    private String fullName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String avatar =
            "https://res.cloudinary.com/dyjdromdd/image/upload/v1791283629/1760239073710_554416787599948448_g1065711509247428827_730f381d0001f3c22e7483cc5b21fef9_zwreqq.jpg";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.CUSTOMER;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(nullable = false)
    private boolean isActive = true;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", nullable = false, unique = true)
    private Address address;
}
