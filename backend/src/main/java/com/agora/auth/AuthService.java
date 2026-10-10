package com.agora.auth;

import com.agora.auth.application.LoginResult;

public interface AuthService {
    LoginResult login(LoginRequest request);
}
