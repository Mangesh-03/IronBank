package com.mangesh.IronBank.service;

import com.mangesh.IronBank.dto.DepositWithdrawRequest;
import com.mangesh.IronBank.dto.DepositWithdrawResponse;
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
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AuditService auditService;

    // Method 1
    @Transactional
    public TransactionResponse transfer(TransactionRequest request, String loggedInEmail) {

        // Step 1: Find fromAccount by accountNumber

        Account fromAccount = accountRepository
                .findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Step 2: IDOR CHECK — verify ownership!

        if(!fromAccount.getUser().getEmail().equals(loggedInEmail))
        {
            //  IDOR attempt caught
            auditService.log(loggedInEmail, "UNAUTHORIZED_TRANSFER", "FAILED", null);

            throw new UnauthorizedAccessException("You don't own this account!");
        }


        // Step 3: Find toAccount by Account Number

        Account toAccount = accountRepository
                .findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(()->new ResourceNotFoundException("Account not found!"));

        // Step 4: Check sufficient balance

        if(fromAccount.getBalance().compareTo(request.getAmount()) < 0)
        {
            // Insufficient funds
            auditService.log(loggedInEmail, "TRANSFER_FAILED", "FAILED", null);

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

        //  Successful transfer
        auditService.log(loggedInEmail, "TRANSFER", "SUCCESS", null);

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

    // Method  2
    public List<TransactionResponse> getTransactionHistory(String accountNumber, String loggedInEmail) {

        // Step 1: Find account by accountNumber
        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Step 2: IDOR( Insecure Direct Object Reference )check
        if (!account.getUser().getEmail().equals(loggedInEmail))
        {
            // IDOR attempt caught
            auditService.log(loggedInEmail, "UNAUTHORIZED_TRANSFER", "FAILED", null);

            throw new UnauthorizedAccessException("You don't own this account!");
        }

        // Step 3: Find all transactions
        List<Transaction> transactions = transactionRepository
                .findByFromAccountOrToAccount(account, account);

        // Step 4: Convert to List<TransactionResponse> using stream
        return transactions.stream()
                .map(txn -> TransactionResponse.builder()
                        .id(txn.getId())
                        .fromAccountNumber(txn.getFromAccount().getAccountNumber())
                        .toAccountNumber(txn.getToAccount().getAccountNumber())
                        .amount(txn.getAmount())
                        .type(txn.getType())
                        .status(txn.getStatus())
                        .description(txn.getDescription())
                        .createdAt(txn.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // Method 3
    @Transactional
    public DepositWithdrawResponse deposit(DepositWithdrawRequest request, String loggedInEmail)
    {
        // step 1 : find account
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(()->new ResourceNotFoundException("Account not found!"));

        // step 2: check is he owner of account
        if(!account.getUser().getEmail().equals(loggedInEmail))
        {
            // IDOR attempt caught
            auditService.log(loggedInEmail, "UNAUTHORIZED_TRANSFER", "FAILED", null);

            throw new UnauthorizedAccessException("Unauthorized access");
        }

        // step 3 : Deposit money
        account.setBalance( account.getBalance().add(request.getAmount()) );

        // step 4 : saved account
        Account saved = accountRepository.save(account);

        // Deposit
        auditService.log(loggedInEmail, "DEPOSIT", "SUCCESS", null);

        // step 5 : send response
        return DepositWithdrawResponse.builder()
                .id(saved.getId())
                .accountNumber(saved.getAccountNumber())
                .type(TransactionType.CREDIT)
                .status(TransactionStatus.SUCCESS)
                .description("Account get credited with amount " + request.getAmount() + " AVL Balance " + saved.getBalance())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    // Method 4
    @Transactional
    public DepositWithdrawResponse withdraw(DepositWithdrawRequest request, String loggedInEmail)
    {
        // step 1 : find account
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(()->new ResourceNotFoundException("Account not found!"));

        // step 2: check is he owner of account
        if(!account.getUser().getEmail().equals(loggedInEmail))
        {
            // IDOR attempt caught
            auditService.log(loggedInEmail, "UNAUTHORIZED_TRANSFER", "FAILED", null);

            throw new UnauthorizedAccessException("Unauthorized access");
        }

        // step 3 : withdraw money

        if(account.getBalance().compareTo(request.getAmount()) < 0)
        {
            // Insufficient funds
            auditService.log(loggedInEmail, "TRANSFER_FAILED", "FAILED", null);

            throw new InsufficientFundsException("Insufficient funds!");
        }

        account.setBalance( account.getBalance().subtract(request.getAmount()) );

        // step 4 : saved account
        Account saved = accountRepository.save(account);

        // Withdraw
        auditService.log(loggedInEmail, "WITHDRAW", "SUCCESS", null);

        // step 5 : send response
        return DepositWithdrawResponse.builder()
                .id(saved.getId())
                .accountNumber(saved.getAccountNumber())
                .type(TransactionType.DEBIT)
                .status(TransactionStatus.SUCCESS)
                .description("Account get Debited with amount " + request.getAmount() + " AVL Balance is "+ saved.getBalance())
                .createdAt(saved.getCreatedAt())
                .build();
    }

}
