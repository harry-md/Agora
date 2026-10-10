package com.agora.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record CategoryRequest(
        @NotBlank @Size(max = 200) String name, @NotNull MultipartFile image, UUID parentId) {}
