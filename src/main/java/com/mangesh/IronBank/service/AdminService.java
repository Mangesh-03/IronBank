package com.mangesh.IronBank.service;

import com.mangesh.IronBank.dto.UserResponse;
import com.mangesh.IronBank.exception.ResourceNotFoundException;
import com.mangesh.IronBank.model.User;
import com.mangesh.IronBank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService
{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    // Method 1: getAllUsers
    public List<UserResponse> getAllUsers()
    {
        // find all users
        List<User> users = userRepository.findAll();

        // convert to UserResponse (never expose password!)
        // return list
        // getAllUsers - add stream

        // Admin viewed all users
        auditService.log("admin@ironbank.com", "VIEW_ALL_USERS", "SUCCESS", null);

        return users.stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .fullName(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .isLocked(user.isLocked())
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // Method 2: unlockAccount
    public String unlockAccount(String email)
    {
        // find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new ResourceNotFoundException("User not found"));

        if(!user.isLocked())
        {
            return "Already Unlock";
        }

        // set isLocked = false
        user.setLocked(false);

        // set failedAttempts = 0
        user.setFailedAttempts(0);

        // set lockedAt = null
        user.setLockedAt(null);

        // save user
        userRepository.save(user);

        // Account unlocked by admin
        auditService.log(email, "ACCOUNT_UNLOCKED", "SUCCESS", null);

        // return success message
        return "Unlocked Successfully !";
    }
}
