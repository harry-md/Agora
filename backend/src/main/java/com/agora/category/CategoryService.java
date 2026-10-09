package com.agora.category;

import java.util.List;

public interface CategoryService {
    CategoryResponse getCategoryBySlug(String slug);

    List<CategoryResponse> getCategories();

    CategoryResponse addCategory(CategoryRequest request);
}
