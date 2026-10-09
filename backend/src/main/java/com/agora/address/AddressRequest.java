package com.agora.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AddressRequest(
        @NotBlank @Size(max = 255) String address,
        @NotNull UUID wardId,
        @NotNull UUID provinceId,
        @NotNull UUID countryId) {}
