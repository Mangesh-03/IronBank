package com.mangesh.IronBank.repository;

import com.mangesh.IronBank.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import  java.util.List;

public interface AuditRepository extends JpaRepository<AuditLog,Long>
{
    List<AuditLog> findByEmail(String email );
}
