package com.agora.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSpecDefinitionRequest(
        @NotBlank @Size(max = 255) String code,
        @NotBlank @Size(max = 255) String name,
        @NotNull SpecDataType dataType,
        @Size(max = 255) String unit) {

    public CreateSpecDefinitionRequest {
        code = code == null ? null : code.strip();
        name = name == null ? null : name.strip();
        unit = unit == null || unit.isBlank() ? null : unit.strip();
    }
}
