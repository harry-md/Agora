package com.agora.auth;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "Tên đăng nhập không được để trống")
        String username,

        @NotNull(message = "Mật khẩu không được để trống") String password) {}
