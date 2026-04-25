package com.hackathon.favoritepayee.service;

import com.hackathon.favoritepayee.dto.FavoriteAccountRequest;
import com.hackathon.favoritepayee.dto.FavoriteAccountResponse;
import com.hackathon.favoritepayee.dto.PageResponse;

public interface FavoriteAccountService {
    FavoriteAccountResponse createFavoriteAccount(FavoriteAccountRequest request);
    PageResponse<FavoriteAccountResponse> getFavoriteAccounts(int page, int size);
    FavoriteAccountResponse updateFavoriteAccount(Long id, FavoriteAccountRequest request);
    void deleteFavoriteAccount(Long id);
}
