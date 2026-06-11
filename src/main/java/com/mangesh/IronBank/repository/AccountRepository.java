package com.mangesh.IronBank.repository;

import com.mangesh.IronBank.model.Account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account,Long>
{
    List<Account> findByUserId(Long id);
}
