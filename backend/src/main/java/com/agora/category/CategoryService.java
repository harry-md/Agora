package com.agora.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CategoryService {

    CategoryResponse getBySlug(String slug);

    Page<CategoryResponse> getAll(Pageable pageable);

    CategoryResponse create(CreateCategoryRequest request);

    Page<CategoryAdminResponse> getAllForAdmin(Pageable pageable);

    public boolean existsById(UUID id);
}
