package com.agora.auth.application;

import com.agora.auth.AuthService;
import com.agora.auth.LoginRequest;
import com.agora.exception.BadRequestException;
import com.agora.security.CustomUser;
import com.agora.security.TokenService;
import com.nimbusds.jose.JOSEException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Override
    public LoginResult login(LoginRequest request) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            request.username(), request.password()));
            CustomUser principal = (CustomUser) authentication.getPrincipal();

            String token = tokenService.generateToken(principal);
            AuthResponse authResponse = new AuthResponse(
                    principal.getUsername(),
                    principal.getFullName(),
                    principal.getAvatar(),
                    principal.getRole());
            return new LoginResult(token, authResponse);
        } catch (BadCredentialsException ex) {
            throw new BadRequestException("Tên đăng nhập hoặc mật khẩu không chính xác!");
        } catch (JOSEException ex) {
            throw new RuntimeException("Có lỗi xảy ra");
        }
    }
}
