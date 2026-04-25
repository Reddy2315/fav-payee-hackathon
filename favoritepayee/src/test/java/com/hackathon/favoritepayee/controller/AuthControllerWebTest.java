package com.hackathon.favoritepayee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.favoritepayee.config.SecurityConfig;
import com.hackathon.favoritepayee.dto.AuthDTOs.LoginRequest;
import com.hackathon.favoritepayee.dto.AuthDTOs.LoginResult;
import com.hackathon.favoritepayee.dto.AuthDTOs.LoginResponse;
import com.hackathon.favoritepayee.exception.GlobalExceptionHandler;
import com.hackathon.favoritepayee.security.JwtAuthFilter;
import com.hackathon.favoritepayee.security.JwtService;
import com.hackathon.favoritepayee.service.inter.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class})
class AuthControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Test
    void loginReturnsHeadersAndBody() throws Exception {
        when(authService.login(new LoginRequest(12L))).thenReturn(new LoginResult(
                "access-token",
                "refresh-token",
                LoginResponse.of(12L, "Alice", 900_000L, 604_800_000L)
        ));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(12L))))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer access-token"))
                .andExpect(header().string("X-Refresh-Token", "refresh-token"))
                .andExpect(jsonPath("$.customerId").value(12))
                .andExpect(jsonPath("$.customerName").value("Alice"));
    }

    @Test
    void loginValidatesRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Customer ID must be a positive number"));
    }

    @Test
    void refreshEndpointIsBlockedWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-token\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutEndpointIsBlockedWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-token\"}"))
                .andExpect(status().isForbidden());
    }
}
