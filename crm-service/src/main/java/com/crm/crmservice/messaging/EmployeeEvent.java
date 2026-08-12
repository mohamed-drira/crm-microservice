package com.crm.crmservice.messaging;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Local copy of the EmployeeEvent from hr-service for deserialization.
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