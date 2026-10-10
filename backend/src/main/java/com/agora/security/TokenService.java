package com.agora.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;

public interface TokenService {
    String generateToken(CustomUser principal) throws JOSEException;

    JWTClaimsSet validateTokenAndGetClaims(String token) throws Exception;
}
