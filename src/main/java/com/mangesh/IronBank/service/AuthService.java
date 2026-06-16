package com.mangesh.IronBank.service;

import com.mangesh.IronBank.dto.LoginRequest;
import com.mangesh.IronBank.dto.LoginResponse;
import com.mangesh.IronBank.dto.RegisterRequest;
import com.mangesh.IronBank.dto.RegisterResponse;
import com.mangesh.IronBank.exception.AccountLockedException;
import com.mangesh.IronBank.exception.DuplicateResourceException;
import com.mangesh.IronBank.exception.InvalidCredentialsException;
import com.mangesh.IronBank.exception.ResourceNotFoundException;
import com.mangesh.IronBank.model.Role;
import com.mangesh.IronBank.model.User;
import com.mangesh.IronBank.repository.UserRepository;
import com.mangesh.IronBank.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // Method 1: register
    @Transactional
    public RegisterResponse register(RegisterRequest request)
    {
        // Step 1: Check if email already exists

        if(userRepository.existsByEmail(request.getEmail()))
        {
            throw new DuplicateResourceException("Email already registered!");
        }

        // Step 2: Hash the password
        String hashPass = passwordEncoder.encode(request.getPassword());

        // Step 3: Build User entity and save

        // Instead of chaining setters
        User user = User.builder()
                .name(request.getFullName())
                .email(request.getEmail())
                .password(hashPass)
                .role(Role.USER)        // default role
                .isLocked(false)
                .failedAttempts(0)
                .build();

        User savedUser = userRepository.save(user);

        // Step 4: Return RegisterResponse
        return RegisterResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getName())
                .email(savedUser.getEmail())
                .message("User registered successfully!")
                .build();

    }

    // Method 2: login
    @Transactional(dontRollbackOn = InvalidCredentialsException.class)
    public LoginResponse login(LoginRequest request)
    {
        // Step 1: Find user by email (throw exception if not found)
        User savedUser = userRepository.findByEmail(request.getEmail()).orElseThrow(()-> new ResourceNotFoundException("User not found !"));

        // Check if account is locked FIRST
        if(savedUser.isLocked())
        {
            System.out.println("Account Locked");
            if(savedUser.getLockedAt().plusMinutes(30).isAfter(LocalDateTime.now()))
            {
                throw new AccountLockedException("Account locked. Try after 30 minutes!");
            }
            else
            {
                // auto unlock after 30 min
                savedUser.setLocked(false);
                savedUser.setFailedAttempts(0);
                savedUser.setLockedAt(null);
            }
        }

        // Step 2: Check password matches using passwordEncoder.matches()
        if( !passwordEncoder.matches(request.getPassword(),savedUser.getPassword()))
        {
            System.out.println("Value of failed attempts 1st: " + savedUser.getFailedAttempts());
            savedUser.setFailedAttempts(savedUser.getFailedAttempts() + 1);

            if(savedUser.getFailedAttempts() >= 5)
            {
                savedUser.setLocked(true);
                savedUser.setLockedAt(LocalDateTime.now());  // ← set lockout time!

            }

            User chk = userRepository.save(savedUser);
            System.out.println("Value of failed attempts after change : " + chk.getFailedAttempts());
            throw new InvalidCredentialsException("Invalid credentials!");
        }

        // If login SUCCESS — reset counter
        savedUser.setFailedAttempts(0);
        userRepository.save(savedUser);

        // Step 3: Generate JWT token
        UserDetails userDetails = customUserDetailsService
                .loadUserByUsername(savedUser.getEmail());
        String jwtToken = jwtTokenProvider.generateToken(userDetails);

        // Step 4: Return LoginResponse with token
        return LoginResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }
}
