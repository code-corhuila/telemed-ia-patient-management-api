package com.telemedai.patient.infrastructure.config.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Rs256VerifierTest {

    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;

    @BeforeAll
    static void generateKeys() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        publicKey = (RSAPublicKey) pair.getPublic();
        privateKey = (RSAPrivateKey) pair.getPrivate();
    }

    private static RSAPrivateKey newPrivateKey() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return (RSAPrivateKey) gen.generateKeyPair().getPrivate();
    }

    private String sign(String sub, String role, Instant exp, JWSAlgorithm alg, RSAPrivateKey key) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(sub)
                .claim("role", role)
                .expirationTime(Date.from(exp))
                .issueTime(new Date())
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(alg), claims);
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-09-26T10:00:00Z"), ZoneOffset.UTC);
    }

    @Test
    void accepts_valid_rs256_token() throws Exception {
        Clock clock = fixedClock();
        Rs256Verifier verifier = new Rs256Verifier(publicKey, clock);
        String token = sign("1001", "PATIENT", clock.instant().plusSeconds(300), JWSAlgorithm.RS256, privateKey);

        AuthenticatedUser user = verifier.verify(token);

        assertThat(user.userId()).isEqualTo(1001L);
        assertThat(user.role()).isEqualTo("PATIENT");
    }

    @Test
    void rejects_token_signed_by_another_key() throws Exception {
        Clock clock = fixedClock();
        Rs256Verifier verifier = new Rs256Verifier(publicKey, clock);

        RSAPrivateKey alienKey = newPrivateKey();
        String alienToken = sign("1001", "PATIENT", clock.instant().plusSeconds(300), JWSAlgorithm.RS256, alienKey);

        assertThatThrownBy(() -> verifier.verify(alienToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("signature");
    }

    @Test
    void rejects_expired_token() throws Exception {
        Clock clock = fixedClock();
        Rs256Verifier verifier = new Rs256Verifier(publicKey, clock);
        String token = sign("1001", "PATIENT", clock.instant().minusSeconds(120), JWSAlgorithm.RS256, privateKey);

        assertThatThrownBy(() -> verifier.verify(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void rejects_hs256_token() throws Exception {
        Clock clock = fixedClock();
        Rs256Verifier verifier = new Rs256Verifier(publicKey, clock);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("1001")
                .expirationTime(Date.from(clock.instant().plusSeconds(300)))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner("01234567890123456789012345678901"));
        String token = jwt.serialize();

        assertThatThrownBy(() -> verifier.verify(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("algorithm");
    }

    @Test
    void rejects_token_without_subject() throws Exception {
        Clock clock = fixedClock();
        Rs256Verifier verifier = new Rs256Verifier(publicKey, clock);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(Date.from(clock.instant().plusSeconds(300)))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner(privateKey));

        assertThatThrownBy(() -> verifier.verify(jwt.serialize()))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("sub");
    }

    @Test
    void rejects_non_numeric_subject() throws Exception {
        Clock clock = fixedClock();
        Rs256Verifier verifier = new Rs256Verifier(publicKey, clock);
        String token = sign("not-a-number", "PATIENT", clock.instant().plusSeconds(300), JWSAlgorithm.RS256, privateKey);

        assertThatThrownBy(() -> verifier.verify(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("sub");
    }

    @Test
    void rejects_malformed_token() {
        Rs256Verifier verifier = new Rs256Verifier(publicKey);
        assertThatThrownBy(() -> verifier.verify("not-a-jwt"))
                .isInstanceOf(InvalidTokenException.class);
    }
}