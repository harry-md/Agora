package com.agora.category.persistence;

import com.agora.category.CategoryAdminResponse;
import com.agora.category.CategoryResponse;
import com.agora.category.domain.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository
        extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {

    boolean existsBySlug(String slug);

    Optional<Category> findBySlug(String slug);

    @Query(value = """
        SELECT new com.agora.category.CategoryAdminResponse(
            c.id,
            c.name,
            c.slug,
            c.imageUrl,
            c.active,
            c.createdAt,
            c.updatedAt
        )
        FROM Category c
        """, countQuery = """
            SELECT COUNT(c)
            FROM Category c
            """)
    Page<CategoryAdminResponse> findAllAdmin(Pageable pageable);

    @Query(value = """
        SELECT new com.agora.category.CategoryResponse(
            c.id,
            c.name,
            c.slug,
            c.imageUrl
        )
        FROM Category c
        WHERE c.active = true
        """, countQuery = """
            SELECT COUNT(c)
            FROM Category c
            """)
    Page<CategoryResponse> findAllActive(Pageable pageable);
}
