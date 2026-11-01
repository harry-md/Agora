package com.agora.warranty.application;

import com.agora.warranty.CreateWarrantyPolicyRequest;
import com.agora.warranty.WarrantyPolicyResponse;
import com.agora.warranty.domain.WarrantyPolicy;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WarrantyPolicyMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    WarrantyPolicy toEntity(CreateWarrantyPolicyRequest request);

    WarrantyPolicyResponse toResponse(WarrantyPolicy warrantyPolicy);
}
