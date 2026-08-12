package com.crm.payroll.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PayrollRequest {

    @NotNull
    @Positive
    private Long employeeId;

    @NotNull
    private LocalDate periodStart;

    @NotNull
    private LocalDate periodEnd;
}
