package com.hackathon.favoritepayee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "favourite_accounts",
        indexes = {
                @Index(name = "idx_favaccount_customer_id", columnList = "customer_id"),
                @Index(name = "idx_favaccount_bank_code_id", columnList = "bank_code_id")
        }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning side of Customer → FavoriteAccount
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_favaccount_customer")
    )
    private Customer customer;

    // Many FavoriteAccounts → One BankCode (lookup table)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bank_code_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_favaccount_bankcode")
    )
    private BankCode bankCode;   // auto-resolved from IBAN, not user-editable

    @NotBlank(message = "Account name is mandatory")
    @Pattern(
            regexp = "^[a-zA-Z0-9'\\-]+$",
            message = "Account name can only contain letters, numbers, apostrophes (') and hyphens (-)"
    )
    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;
    @NotBlank(message = "IBAN/Account number is mandatory")
    @Pattern(
            regexp = "^[a-zA-Z0-9]+$",
            message = "IBAN/Account number can only contain letters and numbers"
    )
    @Size(
            max = 20,
            message = "IBAN/Account number must not exceed 20 characters"
    )
    @Column(name = "iban", nullable = false, length = 20)
    private String iban;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavoriteAccount that)) return false;
        return Objects.equals(iban, that.iban) &&
                Objects.equals(customer, that.customer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iban, customer);
    }
}