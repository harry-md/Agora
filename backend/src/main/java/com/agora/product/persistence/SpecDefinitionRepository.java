package com.agora.product.persistence;

import com.agora.product.domain.SpecDefinition;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpecDefinitionRepository extends JpaRepository<SpecDefinition, UUID> {
    boolean existsByCode(String code);
}
