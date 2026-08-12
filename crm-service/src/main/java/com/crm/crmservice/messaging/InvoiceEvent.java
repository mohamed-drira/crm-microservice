package com.crm.crmservice.messaging;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Local copy of the InvoiceEvent from billing-service for deserialization.
 */
@Data
@NoArgsConstructor
public class InvoiceEvent {

    public enum Type {
        INVOICE_CREATED,
        INVOICE_STATUS_UPDATED
    }

    private UUID eventId;
    private Type type;
    private Long invoiceId;
    private String invoiceNumber;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String status;
    private LocalDateTime timestamp;
}