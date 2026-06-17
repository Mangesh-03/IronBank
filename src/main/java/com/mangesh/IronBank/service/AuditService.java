package com.mangesh.IronBank.service;

import com.mangesh.IronBank.model.AuditLog;
import com.mangesh.IronBank.repository.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private AuditRepository auditRepository;

    public void log(String email, String action,String status, String ipAddress)
    {
        // Build AuditLog entity
        AuditLog log = AuditLog.builder()
                .email(email)
                .action(action)
                .status(status)
                .ipAddress(ipAddress)
                .build();
        // Save to DB

        auditRepository.save(log);
    }
}
