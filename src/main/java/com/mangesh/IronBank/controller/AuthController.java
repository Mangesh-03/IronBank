package com.mangesh.IronBank.controller;

import com.mangesh.IronBank.dto.*;
import com.mangesh.IronBank.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

//    @PostMapping("/register")
//    public ResponseEntity<RegisterResponse> register(
//            @Valid @RequestBody RegisterRequest request,  HttpServletRequest httpRequest)
//    {
//        String ip = httpRequest.getRemoteAddr();
//
//        return ResponseEntity.ok(authService.register(request,ip));
//    }

    @PostMapping("/register/initiate")
    public ResponseEntity<RegisterResponse> registerInitiate(@Valid @RequestBody RegisterRequest request,HttpServletRequest httpRequest)
    {
        String ip = httpRequest.getRemoteAddr();
        System.out.println("Inside request");

        return ResponseEntity.ok(authService.registerInitiate(request,ip));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<RegisterResponse> registerVerify(@Valid @RequestBody VerifyRequest request, HttpServletRequest httpRequest)
    {
        String ip = httpRequest.getRemoteAddr();
        String email = request.getEmail();
        String otp = request.getOtp();

        return ResponseEntity.ok(authService.registerVerify(email,otp,ip));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request , HttpServletRequest httpRequest)
    {
        String ip = httpRequest.getRemoteAddr();

        return ResponseEntity.ok(authService.login(request,ip));
    }
}
