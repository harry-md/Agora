package com.agora.product.application;

import com.agora.product.CreateSpecDefinitionRequest;
import com.agora.product.SpecDefinitionResponse;
import com.agora.product.domain.SpecDefinition;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SpecDefinitionMapper {
    @Mapping(target = "id", ignore = true)
    SpecDefinition toEntity(CreateSpecDefinitionRequest request);

    SpecDefinitionResponse toResponse(SpecDefinition entity);
}
