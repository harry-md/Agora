package com.agora.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);

    Page<ProductSummaryResponse> getAllProducts(ProductFilterRequest request, Pageable pageable);
}
