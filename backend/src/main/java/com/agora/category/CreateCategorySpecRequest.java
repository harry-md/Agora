package com.agora.category;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record CreateCategorySpecRequest(
        @NotNull UUID specDefinitionId,
        boolean required,
        boolean filterable,
        @PositiveOrZero int displayOrder) {}
