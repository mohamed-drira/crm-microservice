package com.crm.payroll.messaging;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PayrollEvent {

    public enum Type {
        PAYROLL_CREATED,
        PAYROLL_UPDATED
    }

    public enum Status {
        DRAFT,
        PAID,
        CANCELLED
    }

    private UUID eventId;
    private Type type;
    private Long payrollId;
    private Long employeeId;
    private String employeeName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal baseSalary;
    private BigDecimal netAmount;
    private Status status;
    private LocalDateTime timestamp;

    public static PayrollEvent created(PayrollSnapshot payload) {
        PayrollEvent event = new PayrollEvent();
        event.setType(Type.PAYROLL_CREATED);
        applyPayload(event, payload);
        return event;
    }

    public static PayrollEvent updated(PayrollSnapshot payload) {
        PayrollEvent event = new PayrollEvent();
        event.setType(Type.PAYROLL_UPDATED);
        applyPayload(event, payload);
        return event;
    }

    private static void applyPayload(PayrollEvent event, PayrollSnapshot payload) {
        event.setEventId(UUID.randomUUID());
        event.setPayrollId(payload.payrollId());
        event.setEmployeeId(payload.employeeId());
        event.setEmployeeName(payload.employeeName());
        event.setPeriodStart(payload.periodStart());
        event.setPeriodEnd(payload.periodEnd());
        event.setBaseSalary(payload.baseSalary());
        event.setNetAmount(payload.netAmount());
        event.setStatus(Status.valueOf(payload.status().name()));
        event.setTimestamp(LocalDateTime.now());
    }

    public record PayrollSnapshot(Long payrollId, Long employeeId, String employeeName,
                                  LocalDate periodStart, LocalDate periodEnd,
                                  BigDecimal baseSalary, BigDecimal netAmount,
                                  com.crm.payroll.entity.enums.PayrollStatus status) {

        public static PayrollSnapshot from(com.crm.payroll.entity.Payroll payroll) {
            return new PayrollSnapshot(payroll.getId(), payroll.getEmployeeId(), payroll.getEmployeeName(),
                    payroll.getPeriodStart(), payroll.getPeriodEnd(),
                    payroll.getBaseSalary(), payroll.getNetAmount(), payroll.getStatus());
        }
    }
}
