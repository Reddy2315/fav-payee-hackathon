package com.hackathon.favoritepayee.repository;

import com.hackathon.favoritepayee.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByIdAndCustomerNameIgnoreCase(Long id, String customerName);

    boolean existsById(Long id);
    
}