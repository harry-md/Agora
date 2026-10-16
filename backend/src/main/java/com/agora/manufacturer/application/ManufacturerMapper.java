package com.agora.manufacturer.application;

import com.agora.manufacturer.CreateManufacturerRequest;
import com.agora.manufacturer.ManufacturerAdminResponse;
import com.agora.manufacturer.ManufacturerResponse;
import com.agora.manufacturer.UpdateManufacturerRequest;
import com.agora.manufacturer.domain.Manufacturer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ManufacturerMapper {

    ManufacturerResponse toResponse(Manufacturer manufacturer);

    ManufacturerAdminResponse toAdminResponse(Manufacturer manufacturer, String countryName);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "logoUrl", ignore = true)
    @Mapping(target = "active", ignore = true)
    Manufacturer toEntity(CreateManufacturerRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "logoUrl", ignore = true)
    void updateEntity(UpdateManufacturerRequest request, @MappingTarget Manufacturer manufacturer);
}
