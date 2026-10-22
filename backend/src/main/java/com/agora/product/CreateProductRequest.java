package com.agora.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public record CreateProductRequest(
        @NotBlank @Size(max = 200) String name,
        @NotNull UUID manufacturerId,
        @NotNull UUID categoryId,
        @NotNull MultipartFile thumbnail,
        String description,
        List<MultipartFile> images,
        @Valid List<SpecValueRequest> specs) {
    public CreateProductRequest {
        name = name == null ? null : name.strip();
        description = description == null || description.isBlank() ? null : description.strip();
        images = images == null
                ? List.of()
                : images.stream().filter(f -> f != null && !f.isEmpty()).toList();
        ;
        specs = specs == null ? List.of() : specs;
    }

    public record SpecValueRequest(
            @NotNull UUID specDefinitionId,
            @NotBlank @Size(max = 255) String value) {
        public SpecValueRequest {
            value = value == null ? null : value.strip();
        }
    }
}
