package com.mangesh.IronBank.service;

import com.mangesh.IronBank.dto.AccountResponse;
import com.mangesh.IronBank.dto.CreateAccountRequest;
import com.mangesh.IronBank.exception.ResourceNotFoundException;
import com.mangesh.IronBank.model.Account;
import com.mangesh.IronBank.model.User;
import com.mangesh.IronBank.repository.AccountRepository;
import com.mangesh.IronBank.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, String email)
    {
        // Step 1: Find user by email from DB

        User user  = userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User Not Found"));

        // Step 2: Generate account number using UUID
        String accountNumber = "ACC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        // Step 3: Build Account entity using builder
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .user(user)
                .balance(BigDecimal.ZERO)
                .accountType(request.getAccountType())
                .isActive(true)
                .build();

        // Step 4: Save and return AccountResponse

        Account saveAcc = accountRepository.save(account);

        return AccountResponse.builder()
                .id(saveAcc.getId())
                .accountType(saveAcc.getAccountType())
                .balance(saveAcc.getBalance())
                .accountNumber(saveAcc.getAccountNumber())
                .isActive(saveAcc.isActive())
                .createdAt(saveAcc.getCreatedAt())
                .build();
    }
}
