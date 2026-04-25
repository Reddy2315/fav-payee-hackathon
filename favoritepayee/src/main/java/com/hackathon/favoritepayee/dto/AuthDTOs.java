package com.hackathon.favoritepayee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AuthDTOs {

    public record LoginRequest(
            @NotNull(message = "Customer ID is required")
            @Positive(message = "Customer ID must be a positive number")
            Long customerId
    ) {}

    public record RefreshTokenRequest(
            @NotBlank(message = "Refresh token is required")
            String refreshToken
    ) {}

    public record LoginResponse(
            Long customerId,
            String customerName,
            long accessTokenExpiresInMs,
            long refreshTokenExpiresInMs
    ) {
        public static LoginResponse of(
                Long customerId,
                String customerName,
                long accessTokenExpiresInMs,
                long refreshTokenExpiresInMs
        ) {
            return new LoginResponse(
                    customerId,
                    customerName,
                    accessTokenExpiresInMs,
                    refreshTokenExpiresInMs
            );
        }
    }

    public record LoginResult(
            String accessToken,
            String refreshToken,
            LoginResponse response
    ) {}

    public record RefreshBody(
            long accessTokenExpiresInMs
    ) {}

    public record RefreshTokenResponse(
            String accessToken,
            String tokenType,
            long accessTokenExpiresInMs
    ) {
        public static RefreshTokenResponse of(String accessToken, long accessTokenExpiresInMs) {
            return new RefreshTokenResponse(accessToken, "Bearer", accessTokenExpiresInMs);
        }
    }
}