package com.crm.payroll.entity;

import com.crm.payroll.entity.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payrolls")
@Getter
@Setter
@NoArgsConstructor
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "employee_name", nullable = false, length = 200)
    private String employeeName;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "base_salary", precision = 15, scale = 2, nullable = false)
    private BigDecimal baseSalary;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal bonuses = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayrollStatus status = PayrollStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "payroll", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Fee> fees = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
