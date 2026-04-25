package com.hackathon.favoritepayee;

import com.hackathon.favoritepayee.security.JwtAuthFilter;
import com.hackathon.favoritepayee.security.JwtService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JwtAuthFilterTest {

    @Mock
    JwtService jwtService;

    @Mock
    FilterChain chain;

    AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void filter_setsAuthentication_whenValidToken() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer tok1");

        when(jwtService.extractCustomerId("tok1")).thenReturn(5L);
        when(jwtService.isTokenExpired("tok1")).thenReturn(false);
        when(jwtService.extractCustomerName("tok1")).thenReturn("Eve");

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(5L, SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        verify(chain).doFilter(request, response);
    }

    @Test
    void filter_leavesUnauthenticated_whenNoHeader() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void filter_leavesUnauthenticated_whenTokenExpired() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer expiredToken");

        when(jwtService.extractCustomerId("expiredToken")).thenReturn(5L);
        when(jwtService.isTokenExpired("expiredToken")).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void filter_leavesUnauthenticated_whenCustomerIdIsNull() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer tok2");

        when(jwtService.extractCustomerId("tok2")).thenReturn(null);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void filter_leavesUnauthenticated_whenJwtServiceThrows() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer badToken");

        when(jwtService.extractCustomerId("badToken")).thenThrow(new RuntimeException("malformed token"));

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void filter_leavesUnauthenticated_whenNoBearerPrefix() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Basic someCredentials");

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }
}