package com.mangesh.IronBank.dto;

import com.mangesh.IronBank.model.TransactionStatus;
import com.mangesh.IronBank.model.TransactionType;
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
public class TransactionResponse
{
    private Long id;

    private String fromAccountNumber;
    private String toAccountNumber;

    private BigDecimal amount;

    private TransactionType type;

    private TransactionStatus status;

    private String description;

    private LocalDateTime createdAt;
}
