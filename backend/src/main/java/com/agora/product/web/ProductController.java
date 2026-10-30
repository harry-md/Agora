package com.agora.product.web;

import com.agora.product.*;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(
            @Valid @ModelAttribute CreateProductRequest request) {
        return new ResponseEntity<>(productService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<ProductSummaryResponse>> listSummary(
            @Valid @ModelAttribute ProductFilterRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(request, pageable));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getBySlug(slug));
    }
}
