package com.telemedai.patient.infrastructure.config.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class Rs256Verifier {

    private static final long CLOCK_SKEW_SECONDS = 30;
    private static final List<JWSAlgorithm> ALLOWED_ALGORITHMS = List.of(JWSAlgorithm.RS256);

    private final RSAPublicKey publicKey;
    private final Clock clock;

    public Rs256Verifier(RSAPublicKey publicKey) {
        this(publicKey, Clock.systemUTC());
    }

    public Rs256Verifier(RSAPublicKey publicKey, Clock clock) {
        this.publicKey = publicKey;
        this.clock = clock;
    }

    public AuthenticatedUser verify(String token) {
        SignedJWT jwt;
        try {
            jwt = SignedJWT.parse(token);
        } catch (ParseException e) {
            throw new InvalidTokenException("malformed token");
        }

        JWSHeader header = jwt.getHeader();
        if (header.getAlgorithm() == null || !ALLOWED_ALGORITHMS.contains(header.getAlgorithm())) {
            throw new InvalidTokenException("algorithm not allowed");
        }

        try {
            if (!jwt.verify(new RSASSAVerifier(publicKey))) {
                throw new InvalidTokenException("signature verification failed");
            }
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidTokenException("signature verification failed");
        }

        JWTClaimsSet claims;
        try {
            claims = jwt.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new InvalidTokenException("invalid claims");
        }

        Date exp = claims.getExpirationTime();
        if (exp == null) {
            throw new InvalidTokenException("missing exp");
        }
        Instant threshold = clock.instant().minusSeconds(CLOCK_SKEW_SECONDS);
        if (exp.toInstant().isBefore(threshold)) {
            throw new InvalidTokenException("token expired");
        }

        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new InvalidTokenException("missing sub");
        }

        Long userId;
        try {
            userId = Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new InvalidTokenException("sub must be a numeric user id");
        }

        return new AuthenticatedUser(userId, extractRole(claims));
    }

    private String extractRole(JWTClaimsSet claims) {
        try {
            Object role = claims.getClaim("role");
            if (role instanceof String s && !s.isBlank()) {
                return s;
            }
            List<String> roles = claims.getStringListClaim("roles");
            if (roles != null && !roles.isEmpty()) {
                return roles.get(0);
            }
            return null;
        } catch (ParseException e) {
            return null;
        }
    }
}