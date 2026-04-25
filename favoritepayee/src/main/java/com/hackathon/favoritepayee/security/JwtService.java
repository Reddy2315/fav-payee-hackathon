package com.hackathon.favoritepayee.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token.expiration-ms:900000}") // default 15 mins
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token.expiration-ms:604800000}") // default 7 days
    private long refreshTokenExpirationMs;

    // Access Token

    public String generateAccessToken(Long customerId, String customerName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("customerName", customerName);
        claims.put("type", "access");

        return buildToken(claims, customerId, accessTokenExpirationMs);
    }

    // Refresh Token

    public String generateRefreshToken(Long customerId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return buildToken(claims, customerId, refreshTokenExpirationMs);
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractClaim(token, c -> c.get("type", String.class)));
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractClaim(token, c -> c.get("type", String.class)));
    }

    // Validation

    public boolean isTokenValid(String token, Long customerId) {
        return extractCustomerId(token).equals(customerId) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extraction

    public Long extractCustomerId(String token) {
        return Long.parseLong(extractClaim(token, Claims::getSubject));
    }

    public String extractCustomerName(String token) {
        return extractClaim(token, c -> c.get("customerName", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    //  Private Helpers

    private String buildToken(Map<String, Object> extraClaims, Long customerId, long expirationMs) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(String.valueOf(customerId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}