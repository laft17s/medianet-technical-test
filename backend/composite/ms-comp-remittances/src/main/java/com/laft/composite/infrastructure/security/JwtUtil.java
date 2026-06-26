package com.laft.composite.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET = "SuperSecretKeyForJwtTokenGenerationMedianet123!";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
    
    // 1 Hour expiration
    private final long EXPIRATION_TIME = 3600000;

    public String generateToken(String username, String identification, Long clientId, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("identification", identification)
                .claim("clientId", clientId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
