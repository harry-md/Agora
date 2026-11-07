package com.agora.product.persistence;

import com.agora.product.domain.ProductDetail;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, UUID> {}
