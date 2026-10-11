package com.agora.category.application;

import com.agora.category.CategorySpecResponse;
import com.agora.category.CategorySpecService;
import com.agora.category.persistence.CategorySpecRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategorySpecServiceImpl implements CategorySpecService {

    private final CategorySpecRepository categorySpecRepository;
    private final CategorySpecMapper categorySpecMapper;

    @Override
    public List<CategorySpecResponse> getCategorySpecs(UUID categoryId) {
        return categorySpecRepository.findAllByCategoryId(categoryId).stream()
                .map(categorySpecMapper::toResponse)
                .toList();
    }
}
