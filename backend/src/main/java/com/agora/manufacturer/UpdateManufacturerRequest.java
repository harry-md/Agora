package com.agora.manufacturer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record UpdateManufacturerRequest(
        @NotBlank @Size(max = 200) String name,
        MultipartFile logo,
        String description,
        @NotNull UUID countryId,
        @NotNull Boolean active) {}
