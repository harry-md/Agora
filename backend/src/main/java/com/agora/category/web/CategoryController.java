package com.agora.category.web;

import com.agora.category.*;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
        return ResponseEntity.ok(categoryService.getCategories(pageable));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @ModelAttribute CreateCategoryRequest request) {
        return new ResponseEntity<>(categoryService.addCategory(request), HttpStatus.CREATED);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CategoryAdminResponse>> listAdmin(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return ResponseEntity.ok(categoryService.getCategoriesForAdmin(pageable));
    }

    @GetMapping("/{categoryId}/filters")
    public ResponseEntity<List<CategorySpecResponse>> getFilters(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(categorySpecService.getCategorySpecs(categoryId));
    }
}
