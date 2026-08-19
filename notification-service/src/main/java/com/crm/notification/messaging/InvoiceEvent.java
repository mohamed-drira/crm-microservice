package com.crm.notification.messaging;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvoiceEvent(
        UUID eventId,
        Type type,
        Long invoiceId,
        String invoiceNumber,
        String customerName,
        String customerEmail,
        BigDecimal amount,
        LocalDate issueDate,
        LocalDate dueDate,
        String status,
        LocalDateTime timestamp
) {
    public enum Type {
        INVOICE_CREATED,
        INVOICE_STATUS_UPDATED
    }
}
