package com.mangesh.IronBank.service;

import com.mangesh.IronBank.dto.TransactionRequest;
import com.mangesh.IronBank.dto.TransactionResponse;
import com.mangesh.IronBank.exception.InsufficientFundsException;
import com.mangesh.IronBank.exception.ResourceNotFoundException;
import com.mangesh.IronBank.exception.UnauthorizedAccessException;
import com.mangesh.IronBank.model.Account;
import com.mangesh.IronBank.model.Transaction;
import com.mangesh.IronBank.model.TransactionStatus;
import com.mangesh.IronBank.model.TransactionType;
import com.mangesh.IronBank.repository.AccountRepository;
import com.mangesh.IronBank.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public TransactionResponse transfer(TransactionRequest request, String loggedInEmail) {

        // Step 1: Find fromAccount by accountNumber

        Account fromAccount = accountRepository
                .findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Step 2: IDOR CHECK — verify ownership!

        if(!fromAccount.getUser().getEmail().equals(loggedInEmail))
        {
            throw new UnauthorizedAccessException("You don't own this account!");
        }


        // Step 3: Find toAccount by Account Number

        Account toAccount = accountRepository
                .findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(()->new ResourceNotFoundException("Account not found!"));

        // Step 4: Check sufficient balance

        if(fromAccount.getBalance().compareTo(request.getAmount()) < 0)
        {
            throw new InsufficientFundsException("Low balance");
        }

        // Step 5: Debit fromAccount
        //         balance = balance - amount
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));

        // Step 6: Credit toAccount
        //         balance = balance + amount
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        // Step 7: Save both accounts

        fromAccount = accountRepository.save(fromAccount);
        toAccount =  accountRepository.save(toAccount);

        System.out.println("Balance in fromAccount : " + fromAccount.getBalance());
        System.out.println("Balance in toAccount : " + toAccount.getBalance());

        // Step 8: Save Transaction record

        Transaction transaction = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .build();


        Transaction saved = transactionRepository.save(transaction);

        // Step 9: Return TransactionResponse

        return TransactionResponse.builder()
                .id(saved.getId())
                .fromAccountNumber(fromAccount.getAccountNumber())
                .toAccountNumber(toAccount.getAccountNumber())
                .amount(saved.getAmount())
                .type(saved.getType())
                .status(saved.getStatus())
                .description(saved.getDescription())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
