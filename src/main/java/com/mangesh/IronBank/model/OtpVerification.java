package com.mangesh.IronBank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerification
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String otp;
    private LocalDateTime expiresAt;
    private boolean isUsed;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
