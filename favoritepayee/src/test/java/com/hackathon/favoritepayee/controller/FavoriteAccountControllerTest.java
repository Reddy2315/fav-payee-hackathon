package com.hackathon.favoritepayee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.favoritepayee.dto.FavoriteAccountRequest;
import com.hackathon.favoritepayee.dto.FavoriteAccountResponse;
import com.hackathon.favoritepayee.dto.PageResponse;
import com.hackathon.favoritepayee.exception.GlobalExceptionHandler;
import com.hackathon.favoritepayee.service.FavoriteAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FavoriteAccountControllerTest {

    @Mock
    private FavoriteAccountService favoriteAccountService;

    @InjectMocks
    private FavoriteAccountController favoriteAccountController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(favoriteAccountController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createFavoriteAccountReturnsCreatedResponse() throws Exception {
        FavoriteAccountRequest request = FavoriteAccountRequest.builder()
                .accountName("Salary")
                .iban("GB12NEDS1234567890")
                .build();
        FavoriteAccountResponse response = FavoriteAccountResponse.builder()
                .id(1L)
                .accountName("Salary")
                .iban("GB12NEDS1234567890")
                .bankName("Ned Bank")
                .build();

        when(favoriteAccountService.createFavoriteAccount(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bankName").value("Ned Bank"));
    }

    @Test
    void getFavoriteAccountsReturnsPage() throws Exception {
        PageResponse<FavoriteAccountResponse> response = new PageResponse<>(
                List.of(FavoriteAccountResponse.builder().id(1L).accountName("Salary").build()),
                0,
                5,
                1,
                1,
                true
        );

        when(favoriteAccountService.getFavoriteAccounts(0, 5)).thenReturn(response);

        mockMvc.perform(get("/api/v1/favorites?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].accountName").value("Salary"));
    }

    @Test
    void updateFavoriteAccountReturnsUpdatedBody() throws Exception {
        FavoriteAccountRequest request = FavoriteAccountRequest.builder()
                .accountName("Updated")
                .iban("GB12NEDS1234567890")
                .build();
        FavoriteAccountResponse response = FavoriteAccountResponse.builder()
                .id(7L)
                .accountName("Updated")
                .iban("GB12NEDS1234567890")
                .bankName("Ned Bank")
                .build();

        when(favoriteAccountService.updateFavoriteAccount(eq(7L), eq(request))).thenReturn(response);

        mockMvc.perform(put("/api/v1/favorites/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountName").value("Updated"));
    }

    @Test
    void deleteFavoriteAccountReturnsNoContent() throws Exception {
        doNothing().when(favoriteAccountService).deleteFavoriteAccount(5L);

        mockMvc.perform(delete("/api/v1/favorites/5"))
                .andExpect(status().isNoContent());
    }

    @Test
    void createFavoriteAccountReturnsValidationErrorForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountName\":\"\",\"iban\":\"bad iban\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
