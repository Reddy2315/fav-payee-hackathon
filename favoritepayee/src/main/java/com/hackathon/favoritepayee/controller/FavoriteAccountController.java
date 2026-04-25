package com.hackathon.favoritepayee.controller;

import com.hackathon.favoritepayee.dto.FavoriteAccountRequest;
import com.hackathon.favoritepayee.dto.FavoriteAccountResponse;
import com.hackathon.favoritepayee.dto.PageResponse;
import com.hackathon.favoritepayee.service.FavoriteAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorite Accounts", description = "Favorite Account Management APIs")
public class FavoriteAccountController {

    private final FavoriteAccountService favoriteAccountService;

    @PostMapping
    @Operation(summary = "Create a new favorite account")
    public ResponseEntity<FavoriteAccountResponse> createFavoriteAccount(
            @Valid @RequestBody FavoriteAccountRequest request) {
        FavoriteAccountResponse response = favoriteAccountService.createFavoriteAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get paginated favorite accounts")
    public ResponseEntity<PageResponse<FavoriteAccountResponse>> getFavoriteAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        PageResponse<FavoriteAccountResponse> response = favoriteAccountService.getFavoriteAccounts(page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a favorite account")
    public ResponseEntity<FavoriteAccountResponse> updateFavoriteAccount(
            @PathVariable Long id,
            @Valid @RequestBody FavoriteAccountRequest request) {
        FavoriteAccountResponse response = favoriteAccountService.updateFavoriteAccount(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a favorite account")
    public ResponseEntity<Void> deleteFavoriteAccount(
            @PathVariable Long id) {
        favoriteAccountService.deleteFavoriteAccount(id);
        return ResponseEntity.noContent().build();
    }
}