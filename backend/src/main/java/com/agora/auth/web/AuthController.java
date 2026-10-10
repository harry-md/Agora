package com.agora.auth.web;

import com.agora.auth.AuthService;
import com.agora.auth.LoginRequest;
import com.agora.auth.application.AuthResponse;
import com.agora.auth.application.LoginResult;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${agora.auth.cookie-secure}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request);
        ResponseCookie cookie = ResponseCookie.from("jwt_token", result.token())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api")
                .maxAge(604800)
                .sameSite("Strict")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.authResponse());
    }
}
