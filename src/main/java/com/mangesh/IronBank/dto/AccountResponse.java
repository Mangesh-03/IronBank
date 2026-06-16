package com.mangesh.IronBank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mangesh.IronBank.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse
{
    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;

    @JsonProperty("isActive")
    private boolean isActive;

    private LocalDateTime createdAt;
}
