package com.hackathon.favoritepayee.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bank_code_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankCodeMapping {

    @Id
    @Column(name = "code", length = 4, nullable = false)
    private String code;

    @Column(name = "bank_name", nullable = false)
    private String bankName;
}
