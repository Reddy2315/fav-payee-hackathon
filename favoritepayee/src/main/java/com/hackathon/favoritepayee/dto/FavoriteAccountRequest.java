package com.hackathon.favoritepayee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteAccountRequest {

    @NotBlank(message = "Account name is required")
    @Pattern(regexp = "^[a-zA-Z0-9'-]+$", message = "Account name must contain only alphanumeric characters, spaces, hyphens, and apostrophes")
    private String accountName;

    @NotBlank(message = "IBAN is required")
    @Size(max = 20, message = "IBAN must not exceed 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "IBAN must be alphanumeric only")
    private String iban;
}
