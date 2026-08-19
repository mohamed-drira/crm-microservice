package com.crm.notification.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

public record EmployeeEvent(
        UUID eventId,
        Type type,
        Long employeeId,
        String firstName,
        String lastName,
        String email,
        String department,
        BigDecimal salary,
        String status,
        LocalDateTime timestamp
) {
    public enum Type {
        EMPLOYEE_CREATED,
        EMPLOYEE_UPDATED
    }
}
