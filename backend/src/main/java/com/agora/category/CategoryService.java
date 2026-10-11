package com.agora.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    CategoryResponse getCategoryBySlug(String slug);

    Page<CategoryResponse> getCategories(Pageable pageable);

    CategoryResponse addCategory(CreateCategoryRequest request);

    Page<CategoryAdminResponse> getCategoriesForAdmin(Pageable pageable);
}
