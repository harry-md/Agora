package com.agora.category.application;

import com.agora.category.CategorySpecResponse;
import com.agora.category.domain.CategorySpec;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategorySpecMapper {

    CategorySpecResponse toResponse(CategorySpec entity);
}
