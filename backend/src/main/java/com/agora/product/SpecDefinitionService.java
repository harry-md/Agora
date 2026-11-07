package com.agora.product;

import java.util.UUID;

public interface SpecDefinitionService {
    SpecDefinitionResponse createSpecDefinition(CreateSpecDefinitionRequest request);

    boolean existsById(UUID id);
}
