package com.crm.billing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class InvoiceRequest {

    @NotBlank
    private String customerName;

    @NotBlank
    @Email
    private String customerEmail;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private LocalDate issueDate;

    @NotNull
    private LocalDate dueDate;
}
