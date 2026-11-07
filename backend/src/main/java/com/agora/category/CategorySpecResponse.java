package com.agora.category;

import java.util.UUID;

public record CategorySpecResponse(
        UUID id, boolean required, boolean filterable, int displayOrder) {}
