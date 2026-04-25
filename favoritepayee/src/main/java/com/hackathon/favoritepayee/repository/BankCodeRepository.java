package com.hackathon.favoritepayee.repository;

import com.hackathon.favoritepayee.entity.BankCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankCodeRepository extends JpaRepository<BankCode, Long> {

    Optional<BankCode> findByIbanCode(String ibanCode);
    boolean existsByIbanCode(String ibanCode);
}