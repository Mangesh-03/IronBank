package com.mangesh.IronBank.controller;

import com.mangesh.IronBank.dto.LoginRequest;
import com.mangesh.IronBank.dto.LoginResponse;
import com.mangesh.IronBank.dto.RegisterRequest;
import com.mangesh.IronBank.dto.RegisterResponse;
import com.mangesh.IronBank.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController
{
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody @Valid RegisterRequest request)
    {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request)
    {
        return authService.login(request);
    }
}
