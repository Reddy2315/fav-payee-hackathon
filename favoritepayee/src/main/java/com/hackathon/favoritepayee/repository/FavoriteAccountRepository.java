package com.hackathon.favoritepayee.repository;

import com.hackathon.favoritepayee.entity.FavoriteAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteAccountRepository extends JpaRepository<FavoriteAccount, Long> {
    Page<FavoriteAccount> findAllByCustomerId(Long customerId, Pageable pageable);
    Optional<FavoriteAccount> findByIdAndCustomerId(Long id, Long customerId);
    boolean existsByCustomerIdAndIban(Long customerId, String iban);
    long countByCustomerId(Long customerId);
}
