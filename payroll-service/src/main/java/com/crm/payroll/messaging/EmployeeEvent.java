package com.crm.payroll.messaging;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload of the employee-events topic produced by hr-service.
 * Kept locally so payroll can hydrate its EmployeeSnapshot copies.
 */
@Data
@NoArgsConstructor
public class EmployeeEvent {

    public enum Type {
        EMPLOYEE_CREATED,
        EMPLOYEE_UPDATED
    }

    private UUID eventId;
    private Type type;
    private Long employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private BigDecimal salary;
    private String status;
    private LocalDateTime timestamp;
}
