package com.agora.security.internal;

import com.agora.security.CustomUser;
import com.agora.security.TokenService;
import com.nimbusds.jwt.JWTClaimsSet;

import jakarta.servlet.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private static final String JWT_COOKIE_NAME = "jwt_token";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            try {
                JWTClaimsSet claimsSet = tokenService.validateTokenAndGetClaims(token);
                if (claimsSet != null) {
                    String userId = claimsSet.getStringClaim("userId");
                    String username = claimsSet.getSubject();
                    String fullName = claimsSet.getStringClaim("fullName");
                    String avatar = claimsSet.getStringClaim("avatar");

                    request.setAttribute("username", username);
                    String role = claimsSet.getStringClaim("role");
                    List<SimpleGrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    CustomUser principal = new CustomUser(
                            UUID.fromString(userId),
                            username,
                            "",
                            fullName,
                            avatar,
                            role,
                            authorities);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
