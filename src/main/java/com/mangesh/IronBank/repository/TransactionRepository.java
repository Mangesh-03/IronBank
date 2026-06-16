package com.mangesh.IronBank.repository;

import com.mangesh.IronBank.model.Account;
import com.mangesh.IronBank.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long>
{
    List<Transaction> findByFromAccountIdOrToAccountId(Long fromId, Long toId);
    List<Transaction> findByFromAccountOrToAccount(Account account1, Account account2);
}
