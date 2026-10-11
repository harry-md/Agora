package com.agora.category.application;

import com.agora.category.CategoryAdminResponse;
import com.agora.category.CategoryResponse;
import com.agora.category.CategoryService;
import com.agora.category.CreateCategoryRequest;
import com.agora.category.domain.Category;
import com.agora.category.persistence.CategoryRepository;
import com.agora.exception.ResourceNotFoundException;
import com.agora.media.ImageStorageService;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.Normalizer;

@Service
@RequiredArgsConstructor
class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ImageStorageService imageStorageService;

    @Override
    @Cacheable(cacheNames = "category", key = "#slug")
    public CategoryResponse getCategoryBySlug(String slug) {
        return categoryRepository
                .findBySlug(slug)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
    }

    @Override
    @Cacheable(cacheNames = "categories")
    public Page<CategoryResponse> getCategories(Pageable pageable) {
        return categoryRepository.findAllActive(pageable);
    }

    private String toSlug(String input) {
        String s = input.trim().toLowerCase().replace("đ", "d");
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return s.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private String uniqueSlug(String base) {
        String slug = base;
        while (categoryRepository.existsBySlug(slug)) {
            slug = base + "-" + String.format("%04d", (int) (Math.random() * 10001));
        }
        return slug;
    }

    @Override
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "categories"),
                @CacheEvict(cacheNames = "admin-categories")
            })
    public CategoryResponse addCategory(CreateCategoryRequest request) {
        String imageUrl = imageStorageService.upload(request.image());
        Category category = categoryMapper.toEntity(request);
        category.setSlug(uniqueSlug(toSlug(request.name())));
        category.setImageUrl(imageUrl);

        try {
            return categoryMapper.toResponse(categoryRepository.save(category));
        } catch (Exception ex) {
            imageStorageService.delete(imageUrl);
            throw ex;
        }
    }

    @Override
    @Cacheable(cacheNames = "admin-categories")
    public Page<CategoryAdminResponse> getCategoriesForAdmin(Pageable pageable) {
        return categoryRepository.findAllAdmin(pageable);
    }
}
