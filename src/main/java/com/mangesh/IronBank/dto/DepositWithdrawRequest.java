package com.mangesh.IronBank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositWithdrawRequest
{
    private String accountNumber;
    private BigDecimal amount;
    private String description;
}
