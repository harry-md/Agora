package com.agora.category;

import java.util.List;
import java.util.UUID;

public interface CategorySpecService {
    List<CategorySpecResponse> getAll(UUID categoryId);

    CategorySpecResponse create(UUID categoryId, CreateCategorySpecRequest request);
}
