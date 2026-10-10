package com.agora.auth.application;

public record LoginResult(String token, AuthResponse authResponse) {}
