package com.crm.payroll.entity;

import com.crm.payroll.entity.enums.EmployeeStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Read-only, locally kept copy of an employee, hydrated from the
 * employee-events topic published by hr-service.
 */
@Entity
@Table(name = "employee_snapshots")
@Getter
@Setter
@NoArgsConstructor
public class EmployeeSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false, unique = true)
    private Long employeeId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 100)
    private String department;

    @Column(precision = 15, scale = 2)
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EmployeeStatus status;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
