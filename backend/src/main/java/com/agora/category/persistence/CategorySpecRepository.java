package com.agora.category.persistence;

import com.agora.category.domain.CategorySpec;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategorySpecRepository extends JpaRepository<CategorySpec, UUID> {
    List<CategorySpec> findAllByCategoryId(UUID categoryId);

    boolean existsByCategoryIdAndSpecDefinitionId(UUID categoryId, UUID specDefinitionId);
}
