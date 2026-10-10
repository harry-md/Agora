package com.agora.security.internal;

import com.agora.security.CustomUser;
import com.agora.security.TokenService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
class TokenServiceImpl implements TokenService {
    private final JwtProperties props;

    @Override
    public String generateToken(CustomUser principal) throws JOSEException {
        JWSSigner signer = new MACSigner(props.secret());
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(principal.getUsername())
                .claim("userId", principal.getId())
                .claim("role", principal.getRole())
                .claim("fullName", principal.getFullName())
                .claim("avatar", principal.getAvatar())
                .expirationTime(new Date(System.currentTimeMillis() + props.expiration()))
                .issueTime(new Date())
                .build();
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    private JWTClaimsSet validateToken(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(props.secret());

        if (signedJWT.verify(verifier)) {
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration.after(new Date())) {
                return signedJWT.getJWTClaimsSet();
            }
        }
        return null;
    }

    @Override
    public JWTClaimsSet validateTokenAndGetClaims(String token) throws Exception {
        return validateToken(token);
    }
}
