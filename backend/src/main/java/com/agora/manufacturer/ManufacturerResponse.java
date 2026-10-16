package com.agora.manufacturer;

import java.util.UUID;

public record ManufacturerResponse(
        UUID id, String name, String slug, String logoUrl, String description, UUID countryId) {}
