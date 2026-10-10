package com.agora.user.domain;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_users_username", columnNames = "username"),
            @UniqueConstraint(name = "uq_users_email", columnNames = "email")
        })
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 200)
    private String username;

    @Column(nullable = false, length = 200)
    private String fullName;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(nullable = false)
    private String avatar =
            "https://res.cloudinary.com/dyjdromdd/image/upload/v1791283629/1760239073710_554416787599948448_g1065711509247428827_730f381d0001f3c22e7483cc5b21fef9_zwreqq.jpg";

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.CUSTOMER;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(nullable = true)
    private UUID addressId;
}
