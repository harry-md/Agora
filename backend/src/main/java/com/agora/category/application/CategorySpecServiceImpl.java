package com.agora.category.application;

import com.agora.category.CategorySpecResponse;
import com.agora.category.CategorySpecService;
import com.agora.category.CreateCategorySpecRequest;
import com.agora.category.domain.CategorySpec;
import com.agora.category.persistence.CategoryRepository;
import com.agora.category.persistence.CategorySpecRepository;
import com.agora.exception.ConflictException;
import com.agora.exception.ResourceNotFoundException;
import com.agora.product.SpecDefinitionService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategorySpecServiceImpl implements CategorySpecService {

    private final CategorySpecRepository categorySpecRepository;
    private final CategorySpecMapper categorySpecMapper;
    private final CategoryRepository categoryRepository;
    private final SpecDefinitionService specDefinitionService;

    @Override
    public List<CategorySpecResponse> getAll(UUID categoryId) {
        return categorySpecRepository.findAllByCategoryId(categoryId).stream()
                .map(categorySpecMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategorySpecResponse create(UUID categoryId, CreateCategorySpecRequest request) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Không tìm thấy danh mục");
        }
        if (!specDefinitionService.existsById(request.specDefinitionId())) {
            throw new ResourceNotFoundException("Không tìm thấy định nghĩa thông số");
        }
        if (categorySpecRepository.existsByCategoryIdAndSpecDefinitionId(
                categoryId, request.specDefinitionId())) {
            throw new ConflictException("Thông số đã được gán cho danh mục này");
        }

        CategorySpec categorySpec = CategorySpec.builder()
                .category(categoryRepository.getReferenceById(categoryId))
                .specDefinitionId(request.specDefinitionId())
                .required(request.required())
                .filterable(request.filterable())
                .displayOrder(request.displayOrder())
                .build();
        return categorySpecMapper.toResponse(categorySpecRepository.save(categorySpec));
    }
}
