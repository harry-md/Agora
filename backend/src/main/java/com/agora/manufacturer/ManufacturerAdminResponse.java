package com.agora.manufacturer;

import java.util.UUID;

public record ManufacturerAdminResponse(
        UUID id,
        String name,
        String slug,
        String logoUrl,
        String description,
        UUID countryId,
        String countryName,
        boolean active) {}
