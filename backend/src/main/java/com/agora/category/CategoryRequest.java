package com.agora.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CategoryRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 500) String imageUrl,
        UUID parentId) {}
