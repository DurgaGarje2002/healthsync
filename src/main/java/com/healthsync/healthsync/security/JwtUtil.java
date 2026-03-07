package com.healthsync.healthsync.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // ✅ Set in application.properties:
    //    jwt.secret=yourVeryLongSecretKeyAtLeast256BitsForHS256Security
    //    jwt.expiration=86400000   (24 hours in ms)
    @Value("${jwt.secret:HealthSyncDefaultSecretKeyMustBeAtLeast256BitsLong1234567890}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ─── Generate Token ───────────────────────
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)        // ✅ role embedded in token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ─── Extract Email ────────────────────────
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // ─── Extract Role ─────────────────────────
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // ─── Validate Token ───────────────────────
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ─── Parse Claims (internal) ──────────────
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}