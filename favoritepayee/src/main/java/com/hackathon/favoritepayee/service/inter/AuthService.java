package com.hackathon.favoritepayee.service.inter;

import com.hackathon.favoritepayee.dto.AuthDTOs.*;

public interface AuthService {

    LoginResult login(LoginRequest request);

    RefreshTokenResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}