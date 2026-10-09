package com.agora.user;

import com.agora.address.AddressRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank @Size(max = 255) String email,
        @Size(max = 200) @NotBlank String username,
        @NotBlank @Size(max = 200) String fullName,
        @NotBlank @Size(min = 6, max = 72) String password,
        @NotNull @Valid AddressRequest address) {}
