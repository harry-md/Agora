package com.agora.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {
    ProductResponse create(CreateProductRequest request);

    Page<ProductSummaryResponse> getAll(ProductFilterRequest request, Pageable pageable);

    ProductDetailResponse getBySlug(String slug);

    void delete(UUID id);
}
