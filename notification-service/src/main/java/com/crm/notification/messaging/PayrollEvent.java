package com.crm.notification.messaging;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PayrollEvent(
        UUID eventId,
        Type type,
        Long payrollId,
        Long employeeId,
        String employeeName,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal baseSalary,
        BigDecimal netAmount,
        Status status,
        LocalDateTime timestamp
) {
    public enum Type {
        PAYROLL_CREATED,
        PAYROLL_UPDATED
    }

    public enum Status {
        DRAFT,
        PAID,
        CANCELLED
    }
}
