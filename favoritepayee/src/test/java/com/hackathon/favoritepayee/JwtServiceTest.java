package com.hackathon.favoritepayee;

import com.hackathon.favoritepayee.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    @Test
    void generateAndParseTokens() {
        JwtService jwtService = new JwtService();
        // 32-char secret required by io.jsonwebtoken Keys.hmacShaKeyFor
        ReflectionTestUtils.setField(jwtService, "secretKey", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 60000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationMs", 120000L);

        String access = jwtService.generateAccessToken(123L, "Alice");
        String refresh = jwtService.generateRefreshToken(123L);

        assertNotNull(access);
        assertNotNull(refresh);

        assertTrue(jwtService.isAccessToken(access));
        assertTrue(jwtService.isRefreshToken(refresh));

        assertEquals(123L, jwtService.extractCustomerId(access));
        assertEquals("Alice", jwtService.extractCustomerName(access));

        assertFalse(jwtService.isTokenExpired(access));
        assertFalse(jwtService.isTokenExpired(refresh));

        assertTrue(jwtService.isTokenValid(access, 123L));
    }
}
