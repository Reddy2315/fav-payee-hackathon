package com.hackathon.favoritepayee.repository;

import com.hackathon.favoritepayee.entity.FavouriteAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavouriteAccountRepository extends JpaRepository<FavouriteAccount, Long> {

    Page<FavouriteAccount> findByCustomerId(Long customerId, Pageable pageable);

    Optional<FavouriteAccount> findByIdAndCustomerId(Long id, Long customerId);

    long countByCustomerId(Long customerId);

    boolean existsByCustomerIdAndIban(Long customerId, String iban);

    @Modifying
    @Query("UPDATE FavouriteAccount fa SET fa.isDeleted = true " +
            "WHERE fa.id = :id AND fa.customer.id = :customerId")
    int softDeleteByIdAndCustomerId(@Param("id") Long id,
                                    @Param("customerId") Long customerId);
}