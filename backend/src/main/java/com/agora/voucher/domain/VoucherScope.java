package com.agora.voucher.domain;

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
        name = "voucher_scopes",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_voucher_scope_category",
                    columnNames = {"voucher_id", "category_id"}),
            @UniqueConstraint(
                    name = "uk_voucher_scope_product",
                    columnNames = {"voucher_id", "product_id"})
        })
public class VoucherScope {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Voucher voucher;

    @Column
    private UUID categoryId;

    @Column
    private UUID productId;
}
