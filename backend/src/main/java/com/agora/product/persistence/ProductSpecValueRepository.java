package com.agora.product.persistence;

import com.agora.product.domain.ProductSpecValue;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductSpecValueRepository extends JpaRepository<ProductSpecValue, UUID> {
    @EntityGraph(attributePaths = "specDefinition")
    List<ProductSpecValue> findByProductId(UUID productId);
}
