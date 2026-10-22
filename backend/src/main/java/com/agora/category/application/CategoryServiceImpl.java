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
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.text.Normalizer;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ImageStorageService imageStorageService;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Cacheable(cacheNames = "category", key = "#slug")
    public CategoryResponse getBySlug(String slug) {
        return categoryRepository
                .findBySlug(slug)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
    }

    @Override
    @Cacheable(cacheNames = "categories")
    public Page<CategoryResponse> getAll(Pageable pageable) {
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
                @CacheEvict(cacheNames = "categories", allEntries = true),
                @CacheEvict(cacheNames = "admin-categories", allEntries = true)
            })
    public CategoryResponse create(CreateCategoryRequest request) {
        final String imageUrl = imageStorageService.upload(request.image());

        try {
            return transactionTemplate.execute(status -> {
                Category category = categoryMapper.toEntity(request);
                category.setSlug(uniqueSlug(toSlug(request.name())));
                category.setImageUrl(imageUrl);

                return categoryMapper.toResponse(categoryRepository.save(category));
            });
        } catch (RuntimeException ex) {
            try {
                imageStorageService.delete(imageUrl);
            } catch (RuntimeException | IOException e) {
                ex.addSuppressed(e);
                log.error("Error while trying cleanup category image", e);
            }
            throw ex;
        }
    }

    @Override
    @Cacheable(cacheNames = "admin-categories")
    public Page<CategoryAdminResponse> getAllForAdmin(Pageable pageable) {
        return categoryRepository.findAllAdmin(pageable);
    }

    @Override
    public boolean existsById(UUID id) {
        return categoryRepository.existsById(id);
    }
}
