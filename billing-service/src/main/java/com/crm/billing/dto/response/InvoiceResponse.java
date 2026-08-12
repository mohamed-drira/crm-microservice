package com.crm.billing.dto.response;

import com.crm.billing.entity.Invoice;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String status;
    private LocalDateTime createdAt;

    public static InvoiceResponse from(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .customerName(invoice.getCustomerName())
                .customerEmail(invoice.getCustomerEmail())
                .amount(invoice.getAmount())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus().name())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}
