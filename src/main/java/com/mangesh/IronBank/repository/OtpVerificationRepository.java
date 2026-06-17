package com.mangesh.IronBank.repository;

import com.mangesh.IronBank.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification,Long>
{
    Optional<OtpVerification> findByEmailAndIsUsedFalse(String email);

    void deleteByEmail(String email);
}
