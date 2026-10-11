package com.agora.category;

import java.time.Instant;
import java.util.UUID;

public record CategoryAdminResponse(
        UUID id,
        String name,
        String slug,
        String imageUrl,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {}
