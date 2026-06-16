package com.powerreliability.common.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
    @Value("${jwt.secret:PowerReliability2026SecretKeyDefault}")
    private String secret;
    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private SecretKey getKey() { return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }

    public String generateToken(Long userId, String username, List<String> permissions) {
        return Jwts.builder().subject(userId.toString())
                .claim("username", username).claim("permissions", permissions)
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey()).compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
    }

    public Long getUserId(String token) { return Long.parseLong(parseToken(token).getSubject()); }
    public boolean isTokenValid(String token) { try { parseToken(token); return true; } catch (Exception e) { return false; } }
}
