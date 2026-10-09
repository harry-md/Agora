package com.agora.address;

import java.util.UUID;

public interface AddressService {
    AddressResponse getAddress(UUID id);

    UUID addAddress(AddressRequest request);

    AddressResponse updateAddress(UUID id, AddressRequest request);
}
