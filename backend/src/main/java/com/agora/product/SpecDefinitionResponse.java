package com.agora.product;

import java.util.UUID;

public record SpecDefinitionResponse(
        UUID id, String code, String name, SpecDataType dataType, String unit) {}
