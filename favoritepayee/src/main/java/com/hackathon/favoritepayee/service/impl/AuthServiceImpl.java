package com.hackathon.favoritepayee.service.impl;

import com.hackathon.favoritepayee.dto.AuthDTOs.*;
import com.hackathon.favoritepayee.entity.Customer;
import com.hackathon.favoritepayee.repository.CustomerRepository;
import com.hackathon.favoritepayee.security.JwtService;
import com.hackathon.favoritepayee.service.inter.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository customerRepository;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        log.info("Login attempt for customerId={}", request.customerId());

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> {
                    log.warn("Login failed — customerId={} not found", request.customerId());
                    return new RuntimeException(
                            "Customer with ID " + request.customerId() + " not found"
                    );
                });

        String accessToken  = jwtService.generateAccessToken(customer.getId(), customer.getCustomerName());
        String refreshToken = jwtService.generateRefreshToken(customer.getId());

        log.info("Login successful for customerId={}", customer.getId());

        LoginResponse body = LoginResponse.of(
                customer.getId(),
                customer.getCustomerName(),
                jwtService.getAccessTokenExpirationMs(),
                jwtService.getRefreshTokenExpirationMs()
        );

        return new LoginResult(accessToken, refreshToken, body);
    }

    @Override
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        // Validate it is actually a refresh token and not expired
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Provided token is not a refresh token");
        }
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new RuntimeException("Refresh token has expired, please login again");
        }

        Long customerId    = jwtService.extractCustomerId(refreshToken);
        String customerName = jwtService.extractCustomerName(refreshToken);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        String newAccessToken = jwtService.generateAccessToken(customer.getId(), customer.getCustomerName());

        log.info("Access token refreshed for customerId={}", customerId);

        return RefreshTokenResponse.of(newAccessToken, jwtService.getAccessTokenExpirationMs());
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        log.info("Logout called — client should discard both tokens");
    }
}