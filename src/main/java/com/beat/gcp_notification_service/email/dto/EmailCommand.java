package com.beat.gcp_notification_service.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record EmailCommand(
        @Email @NotBlank String to,
        @NotBlank String subject,
        @NotBlank String body,
        String contentType, // "text/plain" or "text/html", default to text/plain
        String description,
        BigDecimal totalAmount,
        String currency
) {}

