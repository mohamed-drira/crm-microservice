package com.crm.billing.messaging;

import com.crm.billing.entity.Invoice;
import com.crm.billing.entity.enums.InvoiceStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private InvoiceStatus status;
    private LocalDateTime timestamp;

    public static InvoiceEvent created(Invoice invoice) {
        return build(Type.INVOICE_CREATED, invoice);
    }

    public static InvoiceEvent statusUpdated(Invoice invoice) {
        return build(Type.INVOICE_STATUS_UPDATED, invoice);
    }

    private static InvoiceEvent build(Type type, Invoice invoice) {
        InvoiceEvent event = new InvoiceEvent();
        event.setEventId(UUID.randomUUID());
        event.setType(type);
        event.setInvoiceId(invoice.getId());
        event.setInvoiceNumber(invoice.getInvoiceNumber());
        event.setCustomerName(invoice.getCustomerName());
        event.setCustomerEmail(invoice.getCustomerEmail());
        event.setAmount(invoice.getAmount());
        event.setIssueDate(invoice.getIssueDate());
        event.setDueDate(invoice.getDueDate());
        event.setStatus(invoice.getStatus());
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
}
