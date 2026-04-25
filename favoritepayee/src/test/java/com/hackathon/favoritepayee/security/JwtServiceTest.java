package com.hackathon.favoritepayee.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 900_000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationMs", 604_800_000L);
    }

    @Test
    void generateAccessTokenContainsExpectedClaims() {
        String token = jwtService.generateAccessToken(15L, "Alice");

        assertNotNull(token);
        assertEquals(15L, jwtService.extractCustomerId(token));
        assertEquals("Alice", jwtService.extractCustomerName(token));
        assertTrue(jwtService.isAccessToken(token));
        assertFalse(jwtService.isRefreshToken(token));
        assertTrue(jwtService.isTokenValid(token, 15L));
    }

    @Test
    void generateRefreshTokenContainsRefreshType() {
        String token = jwtService.generateRefreshToken(22L);

        assertNotNull(token);
        assertEquals(22L, jwtService.extractCustomerId(token));
        assertTrue(jwtService.isRefreshToken(token));
        assertFalse(jwtService.isAccessToken(token));
        assertEquals(604_800_000L, jwtService.getRefreshTokenExpirationMs());
    }

    @Test
    void isTokenValidReturnsFalseForDifferentCustomer() {
        String token = jwtService.generateAccessToken(15L, "Alice");

        assertFalse(jwtService.isTokenValid(token, 99L));
    }

    @Test
    void expiredTokenParsingRaisesExpiredJwtException() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", -1L);
        String token = jwtService.generateAccessToken(15L, "Alice");

        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenExpired(token));
    }
}
