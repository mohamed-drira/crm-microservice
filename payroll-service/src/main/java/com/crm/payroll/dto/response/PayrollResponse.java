package com.crm.payroll.dto.response;

import com.crm.payroll.entity.Payroll;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PayrollResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal baseSalary;
    private BigDecimal bonuses;
    private BigDecimal deductions;
    private BigDecimal netAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<FeeResponse> fees;

    public static PayrollResponse from(Payroll payroll) {
        return PayrollResponse.builder()
                .id(payroll.getId())
                .employeeId(payroll.getEmployeeId())
                .employeeName(payroll.getEmployeeName())
                .periodStart(payroll.getPeriodStart())
                .periodEnd(payroll.getPeriodEnd())
                .baseSalary(payroll.getBaseSalary())
                .bonuses(payroll.getBonuses())
                .deductions(payroll.getDeductions())
                .netAmount(payroll.getNetAmount())
                .status(payroll.getStatus().name())
                .createdAt(payroll.getCreatedAt())
                .fees(payroll.getFees().stream().map(FeeResponse::from).toList())
                .build();
    }
}
