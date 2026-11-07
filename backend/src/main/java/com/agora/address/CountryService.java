package com.agora.address;

import java.util.UUID;

public interface CountryService {
    boolean existsById(UUID id);

    String getNameById(UUID id);
}
