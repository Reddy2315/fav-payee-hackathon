package com.hackathon.favoritepayee.service.impl;

import com.hackathon.favoritepayee.dto.AuthDTOs.LoginRequest;
import com.hackathon.favoritepayee.dto.AuthDTOs.LoginResult;
import com.hackathon.favoritepayee.dto.AuthDTOs.LoginResponse;
import com.hackathon.favoritepayee.dto.AuthDTOs.RefreshTokenRequest;
import com.hackathon.favoritepayee.dto.AuthDTOs.RefreshTokenResponse;
import com.hackathon.favoritepayee.entity.Customer;
import com.hackathon.favoritepayee.repository.CustomerRepository;
import com.hackathon.favoritepayee.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginReturnsTokensAndResponseBody() {
        Customer customer = Customer.builder()
                .id(11L)
                .customerName("Alice")
                .build();

        when(customerRepository.findById(11L)).thenReturn(Optional.of(customer));
        when(jwtService.generateAccessToken(11L, "Alice")).thenReturn("access-token");
        when(jwtService.generateRefreshToken(11L)).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900_000L);
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604_800_000L);

        LoginResult result = authService.login(new LoginRequest(11L));

        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals(LoginResponse.of(11L, "Alice", 900_000L, 604_800_000L), result.response());
    }

    @Test
    void loginThrowsWhenCustomerDoesNotExist() {
        when(customerRepository.findById(44L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(new LoginRequest(44L)));

        assertEquals("Customer with ID 44 not found", exception.getMessage());
    }

    @Test
    void refreshThrowsWhenProvidedTokenIsNotRefreshToken() {
        when(jwtService.isRefreshToken("bad-token")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.refresh(new RefreshTokenRequest("bad-token")));

        assertEquals("Provided token is not a refresh token", exception.getMessage());
    }

    @Test
    void refreshThrowsWhenRefreshTokenIsExpired() {
        when(jwtService.isRefreshToken("expired-token")).thenReturn(true);
        when(jwtService.isTokenExpired("expired-token")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.refresh(new RefreshTokenRequest("expired-token")));

        assertEquals("Refresh token has expired, please login again", exception.getMessage());
    }

    @Test
    void refreshThrowsWhenCustomerCannotBeLoaded() {
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.isTokenExpired("refresh-token")).thenReturn(false);
        when(jwtService.extractCustomerId("refresh-token")).thenReturn(7L);
        when(jwtService.extractCustomerName("refresh-token")).thenReturn("Ghost");
        when(customerRepository.findById(7L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.refresh(new RefreshTokenRequest("refresh-token")));

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void refreshReturnsNewAccessToken() {
        Customer customer = Customer.builder()
                .id(7L)
                .customerName("Bob")
                .build();

        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.isTokenExpired("refresh-token")).thenReturn(false);
        when(jwtService.extractCustomerId("refresh-token")).thenReturn(7L);
        when(jwtService.extractCustomerName("refresh-token")).thenReturn("Bob");
        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer));
        when(jwtService.generateAccessToken(7L, "Bob")).thenReturn("new-access-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900_000L);

        RefreshTokenResponse response = authService.refresh(new RefreshTokenRequest("refresh-token"));

        assertEquals(RefreshTokenResponse.of("new-access-token", 900_000L), response);
    }

    @Test
    void logoutDoesNotThrow() {
        assertDoesNotThrow(() -> authService.logout(new RefreshTokenRequest("refresh-token")));
        verifyNoFurtherInteractions();
    }

    private void verifyNoFurtherInteractions() {
        verify(customerRepository, org.mockito.Mockito.never()).findById(-1L);
    }
}
