package com.agora.warranty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;

import java.time.Instant;
import java.util.Locale;

public record CreateWarrantyPolicyRequest(
        @NotBlank @Size(max = 100) String code,
        @NotBlank @Size(max = 255) String title,
        @NotNull Instant effectiveFrom,
        @NotBlank @Size(max = 500) @URL(protocol = "https") String documentKey) {
    public CreateWarrantyPolicyRequest {
        code = code == null ? null : code.strip().toUpperCase(Locale.ROOT);
        title = title == null ? null : title.strip();
    }
}
