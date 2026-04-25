package com.hackathon.favoritepayee.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterSkipsWhenHeaderIsMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterSkipsWhenHeaderIsNotBearerToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc");

        jwtAuthFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterSetsAuthenticationForValidToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.extractCustomerId("valid-token")).thenReturn(88L);
        when(jwtService.isTokenExpired("valid-token")).thenReturn(false);
        when(jwtService.extractCustomerName("valid-token")).thenReturn("Alice");

        jwtAuthFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(88L, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_CUSTOMER".equals(authority.getAuthority())));
    }

    @Test
    void doFilterDoesNotAuthenticateExpiredToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer expired-token");

        when(jwtService.extractCustomerId("expired-token")).thenReturn(88L);
        when(jwtService.isTokenExpired("expired-token")).thenReturn(true);

        jwtAuthFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterIgnoresJwtParsingFailures() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer broken-token");

        when(jwtService.extractCustomerId("broken-token")).thenThrow(new RuntimeException("bad token"));

        jwtAuthFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
