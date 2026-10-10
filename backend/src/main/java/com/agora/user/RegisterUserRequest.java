package com.agora.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

public record RegisterUserRequest(
        @Size(max = 200) @NotBlank String username,
        @Email @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(max = 200) String fullName,
        @NotBlank @Size(min = 1, max = 72) String password,
        MultipartFile avatar) {

    public RegisterUserRequest {
        username = username == null ? null : username.trim();
        email = email == null ? null : email.trim();
        fullName = fullName == null ? null : fullName.trim();
    }
}
