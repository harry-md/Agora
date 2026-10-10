package com.agora.user.web;

import com.agora.user.RegisterUserRequest;
import com.agora.user.UserResponse;
import com.agora.user.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> register(
            @Valid @ModelAttribute RegisterUserRequest request) {
        return new ResponseEntity<>(userService.register(request), HttpStatus.CREATED);
    }
}
