package com.agora.product;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String slug,
        UUID manufacturerId,
        UUID categoryId,
        String description,
        String thumbnail,
        List<String> images,
        List<SpecValueResponse> specs) {
    public record SpecValueResponse(
            @JsonUnwrapped SpecDefinitionResponse definition, Object value) {}
}
