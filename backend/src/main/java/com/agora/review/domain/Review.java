package com.agora.review.domain;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
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
        name = "reviews",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_review_user_product",
                        columnNames = {"user_id", "product_id"}))
class Review {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID productId;

    @Column(columnDefinition = "text")
    private String comment;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
}
