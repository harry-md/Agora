package com.agora.category.web;

import com.agora.category.*;

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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategorySpecService categorySpecService;

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return ResponseEntity.ok(categoryService.getAll(pageable));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getBySlug(slug));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponse> create(
            @Valid @ModelAttribute CreateCategoryRequest request) {
        return new ResponseEntity<>(categoryService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CategoryAdminResponse>> listAdmin(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return ResponseEntity.ok(categoryService.getAllForAdmin(pageable));
    }

    @GetMapping("/{categoryId}/filters")
    public ResponseEntity<List<CategorySpecResponse>> getFilters(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(categorySpecService.getAll(categoryId));
    }

    @PostMapping("/{categoryId}/filters")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategorySpecResponse> createFilter(
            @PathVariable UUID categoryId, @Valid @RequestBody CreateCategorySpecRequest request) {
        return new ResponseEntity<>(
                categorySpecService.create(categoryId, request), HttpStatus.CREATED);
    }
}
