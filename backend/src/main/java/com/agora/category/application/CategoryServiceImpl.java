package com.agora.category.application;

import com.agora.category.CategoryRequest;
import com.agora.category.CategoryResponse;
import com.agora.category.CategoryService;
import com.agora.category.domain.Category;
import com.agora.category.persistence.CategoryRepository;
import com.agora.exception.ResourceNotFound;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
@RequiredArgsConstructor
class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse getCategoryBySlug(String slug) {
        return categoryRepository
                .findBySlug(slug)
                .map(categoryMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFound("Không tìm thấy danh mục"));
    }

    @Override
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAllByActive(true).stream()
                .map(categoryMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse addCategory(CategoryRequest request) {
        Category parent = null;
        if (request.parentId() != null) {
            parent = categoryRepository
                    .findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFound("Không tìm thấy cha danh mục"));
        }

        Category c = categoryMapper.toEntity(request);
        c.setSlug(uniqueSlug(toSlug(request.name())));
        c.setParent(parent);
        Category saved = categoryRepository.save(c);
        return categoryMapper.toResponse(saved);
    }

    private String toSlug(String input) {
        String s = input.trim().toLowerCase().replace("đ", "d");
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return s.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private String uniqueSlug(String base) {
        String slug = base;
        int i = 2;
        while (categoryRepository.existsBySlug(slug)) {
            slug = base + "-" + i++;
        }
        return slug;
    }
}
