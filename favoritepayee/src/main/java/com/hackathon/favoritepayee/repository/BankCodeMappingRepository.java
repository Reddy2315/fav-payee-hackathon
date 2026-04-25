package com.hackathon.favoritepayee.repository;

import com.hackathon.favoritepayee.entity.BankCodeMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankCodeMappingRepository extends JpaRepository<BankCodeMapping, String> {
}
