package com.mangesh.IronBank.controller;

import com.mangesh.IronBank.dto.DepositWithdrawRequest;
import com.mangesh.IronBank.dto.DepositWithdrawResponse;
import com.mangesh.IronBank.dto.TransactionRequest;
import com.mangesh.IronBank.dto.TransactionResponse;
import com.mangesh.IronBank.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController
{
    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse>transfer(@Valid @RequestBody TransactionRequest request)
    {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(transactionService.transfer(request,email));
    }

    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>>getTransactionHistory(@PathVariable String accountNumber)
    {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber,email));
    }
    @PostMapping("/deposit")
    public ResponseEntity<DepositWithdrawResponse>deposit(@RequestBody @Valid DepositWithdrawRequest request)
    {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(transactionService.deposit(request,email));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<DepositWithdrawResponse>withdraw(@RequestBody @Valid DepositWithdrawRequest request)
    {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(transactionService.withdraw(request,email));
    }
}
