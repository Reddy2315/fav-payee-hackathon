package com.hackathon.favoritepayee.service;

import com.hackathon.favoritepayee.dto.AuthRequest;
import com.hackathon.favoritepayee.dto.AuthResponse;
import com.hackathon.favoritepayee.entity.Customer;
import com.hackathon.favoritepayee.exception.BusinessException;
import com.hackathon.favoritepayee.repository.CustomerRepository;
import com.hackathon.favoritepayee.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void register(AuthRequest request) {
        if (customerRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username is already taken");
        }

        Customer customer = Customer.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        customerRepository.save(customer);
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder().token(token).build();
    }
}
