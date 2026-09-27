package com.telemedai.patient.infrastructure.config.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {

    @Bean
    public RSAPublicKey jwtPublicKey(JwtProperties properties) {
        String pem = properties.publicKey();
        if (pem == null || pem.isBlank()) {
            throw new IllegalStateException(
                "app.security.jwt.public-key (env JWT_PUBLIC_KEY) is required");
        }
        // The PEM arrives in one line, with literal \n written by the env loader.
        String normalized = pem.replace("\\n", "\n")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        try {
            byte[] decoded = Base64.getDecoder().decode(normalized);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("invalid JWT public key", e);
        }
    }
}