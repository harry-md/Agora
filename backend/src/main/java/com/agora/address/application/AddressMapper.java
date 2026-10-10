package com.agora.address.application;

import com.agora.address.AddressRequest;
import com.agora.address.AddressResponse;
import com.agora.address.domain.Address;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    //    AddressResponse toDTO(Address address);

    @Mapping(target = "ward", source = "ward.name")
    @Mapping(target = "province", source = "province.name")
    @Mapping(target = "country", source = "country.name")
    AddressResponse toResponse(Address address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ward", ignore = true)
    @Mapping(target = "province", ignore = true)
    @Mapping(target = "country", ignore = true)
    Address toEntity(AddressRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ward", ignore = true)
    @Mapping(target = "province", ignore = true)
    @Mapping(target = "country", ignore = true)
    void updateEntity(AddressRequest request, @MappingTarget Address address);
}
