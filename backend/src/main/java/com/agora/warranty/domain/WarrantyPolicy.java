package com.agora.warranty.domain;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(
        name = "warranty_policies",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_warranty_policies_code_version",
                    columnNames = {"code", "version"})
        })
public class WarrantyPolicy {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, updatable = false)
    private int version;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Instant effectiveFrom;

    /**
     * lưu path tới file bảo hành PDF, DOCX, PNG, ... trên Google Drive hoặc Cloudflare R2
     */
    @Column(nullable = false, length = 500)
    private String documentKey;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
}
