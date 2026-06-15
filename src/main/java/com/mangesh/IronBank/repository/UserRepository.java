package com.mangesh.IronBank.repository;

import com.mangesh.IronBank.model.User;  // ← ADD THIS LINE

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>
{
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

}
