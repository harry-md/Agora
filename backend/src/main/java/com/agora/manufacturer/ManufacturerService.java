package com.agora.manufacturer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ManufacturerService {

    ManufacturerResponse getBySlug(String slug);

    Page<ManufacturerAdminResponse> getAllForAdmin(Pageable pageable);

    ManufacturerResponse create(CreateManufacturerRequest request);

    ManufacturerAdminResponse update(UUID id, UpdateManufacturerRequest request);
}
