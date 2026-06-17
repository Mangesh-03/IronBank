package com.mangesh.IronBank.service;

import com.mangesh.IronBank.dto.LoginRequest;
import com.mangesh.IronBank.dto.LoginResponse;
import com.mangesh.IronBank.dto.RegisterRequest;
import com.mangesh.IronBank.dto.RegisterResponse;
import com.mangesh.IronBank.exception.*;
import com.mangesh.IronBank.model.AuditLog;
import com.mangesh.IronBank.model.OtpVerification;
import com.mangesh.IronBank.model.Role;
import com.mangesh.IronBank.model.User;
import com.mangesh.IronBank.repository.OtpVerificationRepository;
import com.mangesh.IronBank.repository.UserRepository;
import com.mangesh.IronBank.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

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

    @Autowired
    private AuditService auditService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpVerificationRepository otpVerificationRepository;

    @Value("${otp.expiration}")
    private long otpExpiration;

    // Method 1: register
    @Transactional
    public RegisterResponse registerInitiate(RegisterRequest request,String ipAddress)
    {
        // Step 1: Check if email already exists

        if(userRepository.existsByEmail(request.getEmail())) {
            if(customUserDetailsService.getUserForVerfication(request.getEmail())) {
                // Fully verified — block them
                throw new DuplicateResourceException("Email already registered and verified!");
            } else {
                // Exists but unverified — resend OTP, don't create new user
                String newOTP = String.valueOf(otpGenerator());

                // Delete old OTP records for this email
                otpVerificationRepository.deleteByEmail(request.getEmail());

                // Save new OTP
                OtpVerification otpVerify = OtpVerification.builder()
                        .isUsed(false)
                        .otp(newOTP)
                        .email(request.getEmail())
                        .expiresAt(LocalDateTime.now().plusMinutes(10))
                        .build();
                otpVerificationRepository.save(otpVerify);

                // Resend email
                emailService.sendOtpEmail(request.getEmail(), newOTP);

                auditService.log(request.getEmail(), "OTP_RESEND", "SUCCESS", ipAddress);

                // Return early — don't create new user!
                return RegisterResponse.builder()
                        .email(request.getEmail())
                        .message("New OTP sent! Please verify your account.")
                        .build();
            }
        }

        // Step 2: Hash the password
        String hashPass = passwordEncoder.encode(request.getPassword());

        // Step 3: Build User entity and save ,send otp to mail for vefication

        String OTP = String.valueOf(otpGenerator());
        // Email send
        emailService.sendOtpEmail(request.getEmail(),OTP);

        // saved otp to DB

        OtpVerification otpVerify = OtpVerification.builder()
                        .isUsed(false)
                        .otp(OTP)
                        .email(request.getEmail())
                        .expiresAt(LocalDateTime.now().plusMinutes(10))
                        .build();



        otpVerify = otpVerificationRepository.save(otpVerify);

        User user = User.builder()
                .name(request.getFullName())
                .email(request.getEmail())
                .password(hashPass)
                .role(Role.USER)        // default role
                .isLocked(false)
                .isVerified(false)
                .failedAttempts(0)
                .build();

        User savedUser = userRepository.save(user);

        // Rgistered successfully
        auditService.log(request.getEmail(), "OTP_SEND", "SUCCESS", ipAddress);

        // Step 4: Return RegisterResponse
        return RegisterResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getName())
                .email(savedUser.getEmail())
                .message("OTP send to your mail , Please verify your account!")
                .build();

    }

    public RegisterResponse registerVerify(String email,String otp,String ipAddress)
    {
        // Step 1: Check if email already exists

        if(userRepository.existsByEmail(email) )
        {
            if(customUserDetailsService.getUserForVerfication(email) )
            {
                throw new DuplicateResourceException("Email already registered and verified !");
            }
        }

        // Step 2: Get user from DB
        User savedUser = userRepository.findByEmail(email)
                .orElseThrow(()->new ResourceNotFoundException("User not found"));

        OtpVerification otpVerify = otpVerificationRepository.findByEmailAndIsUsedFalse(email)
                        .orElseThrow(()->new InvalidCredentialsException("Invalid Credential!"));

        if(otpVerify.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("OTP has expired!");
        }

        if(!otpVerify.getOtp().equals(otp))
        {
            throw new InvalidCredentialsException("Invalid otp!");
        }

        savedUser.setVerified(true);

        userRepository.save(savedUser);

        // Rgistered successfully
        auditService.log(email, "REGISTER", "SUCCESS", ipAddress);

        emailService.welCome(email);

        // Step 4: Return RegisterResponse
        return RegisterResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getName())
                .email(savedUser.getEmail())
                .message("Register successfully!")
                .build();

    }

    // Method 2: login
    @Transactional(dontRollbackOn = InvalidCredentialsException.class)
    public LoginResponse login(LoginRequest request,String ip)
    {
        // Step 1: Find user by email (throw exception if not found)
        User savedUser = userRepository.findByEmail(request.getEmail()).orElseThrow(()-> new ResourceNotFoundException("User not found !"));

        if(!savedUser.isVerified())
        {
            throw new UnauthorizedAccessException("Please verify your email first!!");
        }

        // Check if account is locked FIRST
        if(savedUser.isLocked())
        {
            System.out.println("Account Locked");

            // Account already locked
            auditService.log(request.getEmail(), "LOGIN_BLOCKED", "FAILED", ip);

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
            // failed attempt
            auditService.log(request.getEmail(), "FAILED_LOGIN", "FAILED", ip);

            System.out.println("Value of failed attempts 1st: " + savedUser.getFailedAttempts());
            savedUser.setFailedAttempts(savedUser.getFailedAttempts() + 1);

            if(savedUser.getFailedAttempts() >= 5)
            {
                savedUser.setLocked(true);

                // Account lock
                auditService.log(request.getEmail(), "ACCOUNT_LOCKED", "FAILED", ip);

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

        // On successful login
        auditService.log(request.getEmail(), "LOGIN", "SUCCESS", ip);

        // Step 4: Return LoginResponse with token
        return LoginResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

    // Method 5  : for otp generator
    public int otpGenerator()
    {
            Random rand = new Random();
            return 100000 + rand.nextInt(900000);

    }
}
