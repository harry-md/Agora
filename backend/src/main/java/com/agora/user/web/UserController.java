package com.agora.user.web;

import com.agora.address.AddressRequest;
import com.agora.address.AddressResponse;
import com.agora.user.RegisterRequest;
import com.agora.user.UserResponse;
import com.agora.user.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @GetMapping("/{userId}/address")
    public AddressResponse getAddress(@PathVariable UUID userId) {
        return userService.getAddress(userId);
    }

    @PutMapping("/{userId}/address")
    public AddressResponse updateAddress(
            @PathVariable UUID userId, @Valid @RequestBody AddressRequest request) {
        return userService.updateAddress(userId, request);
    }
}
