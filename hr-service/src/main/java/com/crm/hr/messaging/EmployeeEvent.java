package com.crm.hr.messaging;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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

    public static EmployeeEvent created(Long employeeId, String firstName, String lastName,
                                        String email, String department, BigDecimal salary, String status) {
        return build(Type.EMPLOYEE_CREATED, employeeId, firstName, lastName, email, department, salary, status);
    }

    public static EmployeeEvent updated(Long employeeId, String firstName, String lastName,
                                        String email, String department, BigDecimal salary, String status) {
        return build(Type.EMPLOYEE_UPDATED, employeeId, firstName, lastName, email, department, salary, status);
    }

    public static EmployeeEvent build(Type type, Long employeeId, String firstName, String lastName,
                                      String email, String department, BigDecimal salary, String status) {
        EmployeeEvent event = new EmployeeEvent();
        event.setEventId(UUID.randomUUID());
        event.setType(type);
        event.setEmployeeId(employeeId);
        event.setFirstName(firstName);
        event.setLastName(lastName);
        event.setEmail(email);
        event.setDepartment(department);
        event.setSalary(salary);
        event.setStatus(status);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
}
