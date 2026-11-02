package com.agora.warranty;

import java.time.Instant;
import java.util.UUID;

public record WarrantyPolicyResponse(
        UUID id,
        String code,
        int version,
        String title,
        Instant effectiveFrom,
        String documentKey) {}
