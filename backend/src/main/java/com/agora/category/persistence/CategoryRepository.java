package com.agora.category.persistence;

import com.agora.category.domain.Category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByActive(boolean active);

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
