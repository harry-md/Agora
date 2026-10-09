package com.agora.user;

import com.agora.address.AddressRequest;
import com.agora.address.AddressResponse;

import java.util.UUID;

public interface UserService {
    boolean checkUsernameExist(String username);

    UserResponse register(RegisterRequest request);

    AddressResponse getAddress(UUID userId);

    AddressResponse updateAddress(UUID userId, AddressRequest request);
}
