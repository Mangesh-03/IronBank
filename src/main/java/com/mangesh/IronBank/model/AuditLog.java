package com.mangesh.IronBank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuditLog
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String action;

    private String ipAddress;

    private String status;


    @CreationTimestamp
    private LocalDateTime createdAt;

}
