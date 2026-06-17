package com.mangesh.IronBank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyRequest
{
    @NotBlank(message = "OTP required")
    private String otp;

    @NotBlank(message = "Email required")
    private String email;
}
