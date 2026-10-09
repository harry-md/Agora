package com.agora.category.application;

import com.agora.category.CategoryRequest;
import com.agora.category.CategoryResponse;
import com.agora.category.domain.Category;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toDTO(Category category);

    CategoryResponse toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "parent", ignore = true)
    Category toEntity(CategoryRequest request);
}
