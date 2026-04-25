package com.hackathon.favoritepayee;

import com.hackathon.favoritepayee.dto.AuthDTOs.*;
import com.hackathon.favoritepayee.entity.Customer;
import com.hackathon.favoritepayee.repository.CustomerRepository;
import com.hackathon.favoritepayee.security.JwtService;
import com.hackathon.favoritepayee.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    CustomerRepository customerRepository;

    @Mock
    JwtService jwtService;

    @InjectMocks
    AuthServiceImpl authService;

    @Test
    void login_success() {
        Customer c = new Customer();
        c.setId(1L);
        c.setCustomerName("Bob");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(jwtService.generateAccessToken(1L, "Bob")).thenReturn("access-token");
        when(jwtService.generateRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(60000L);
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(120000L);

        LoginRequest req = new LoginRequest(1L);
        LoginResult result = authService.login(req);

        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertNotNull(result.response());
        assertEquals(1L, result.response().customerId());
        assertEquals("Bob", result.response().customerName());
    }

    @Test
    void refresh_success() {
        when(jwtService.isRefreshToken("r1")).thenReturn(true);
        when(jwtService.isTokenExpired("r1")).thenReturn(false);
        when(jwtService.extractCustomerId("r1")).thenReturn(2L);
        when(jwtService.extractCustomerName("r1")).thenReturn("Clara");

        Customer c = new Customer();
        c.setId(2L);
        c.setCustomerName("Clara");
        when(customerRepository.findById(2L)).thenReturn(Optional.of(c));

        when(jwtService.generateAccessToken(2L, "Clara")).thenReturn("new-access");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(60000L);

        RefreshTokenRequest req = new RefreshTokenRequest("r1");
        RefreshTokenResponse resp = authService.refresh(req);

        assertEquals("new-access", resp.accessToken());
        assertEquals(60000L, resp.accessTokenExpiresInMs());
    }

    @Test
    void refresh_invalidToken_throws() {
        when(jwtService.isRefreshToken("bad")).thenReturn(false);

        RefreshTokenRequest req = new RefreshTokenRequest("bad");
        assertThrows(RuntimeException.class, () -> authService.refresh(req));
    }
}
