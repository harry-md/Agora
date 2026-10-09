package com.agora.category.web;

import com.agora.category.CategoryResponse;
import com.agora.category.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/")
    public List<CategoryResponse> getCategories() {
        return categoryService.getCategories();
    }

    @GetMapping("/{slug}")
    public CategoryResponse getCategoryBySlug(@PathVariable String slug) {
        return categoryService.getCategoryBySlug(slug);
    }
}
