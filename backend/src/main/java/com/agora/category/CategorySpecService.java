package com.agora.category;

import java.util.List;
import java.util.UUID;

public interface CategorySpecService {
    List<CategorySpecResponse> getCategorySpecs(UUID categoryId);
}
