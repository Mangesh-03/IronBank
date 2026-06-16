package com.mangesh.IronBank.controller;

import com.mangesh.IronBank.dto.AccountResponse;
import com.mangesh.IronBank.dto.CreateAccountRequest;
import com.mangesh.IronBank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController
{
    @Autowired
    private AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request)
    {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(accountService.createAccount(request,email));
    }
}
