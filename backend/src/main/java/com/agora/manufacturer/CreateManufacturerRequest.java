package com.agora.manufacturer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record CreateManufacturerRequest(
        @NotBlank @Size(max = 200) String name,
        @NotNull MultipartFile logo,
        String description,
        @NotNull UUID countryId) {}
