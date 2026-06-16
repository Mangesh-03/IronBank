package com.mangesh.IronBank.controller;

import com.mangesh.IronBank.dto.UserResponse;
import com.mangesh.IronBank.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController
{
    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>>getAllUsers()
    {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/unlock/{email}")
    public ResponseEntity<String> unlockAccount(@PathVariable String email)
    {
        System.out.println("Inside unlock controller");
        return ResponseEntity.ok(adminService.unlockAccount(email));
    }
}
