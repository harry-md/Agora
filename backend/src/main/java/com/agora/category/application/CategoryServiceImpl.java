package com.agora.category.application;

import com.agora.category.CategoryRequest;
import com.agora.category.CategoryResponse;
import com.agora.category.CategoryService;
import com.agora.category.domain.Category;
import com.agora.category.persistence.CategoryRepository;
import com.agora.exception.BadRequestException;
import com.agora.exception.ResourceNotFoundException;
import com.agora.media.ImageStorageService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ImageStorageService imageStorageService;

    @Override
    public CategoryResponse getCategoryBySlug(String slug) {
        return categoryRepository
                .findBySlug(slug)
                .map(categoryMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
    }

    @Override
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAllByActive(true).stream()
                .map(categoryMapper::toDTO)
                .toList();
    }

    @Override
    public CategoryResponse addCategory(CategoryRequest request) {
        UUID parentId = request.parentId();
        if (parentId != null) {
            if (!categoryRepository.existsById(parentId))
                throw new BadRequestException("Không tìm thấy danh mục cha");
        }

        String imageUrl = imageStorageService.upload(request.image());
        Category category = categoryMapper.toEntity(request);
        category.setSlug(uniqueSlug(toSlug(request.name())));
        if (parentId != null) {
            category.setParent(categoryRepository.getReferenceById(parentId));
        }
        category.setImageUrl(imageUrl);

        try {
            return categoryMapper.toDTO(categoryRepository.save(category));
        } catch (Exception ex) {
            imageStorageService.delete(imageUrl);
            throw ex;
        }
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
}
