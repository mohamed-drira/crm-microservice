package com.crm.payroll.dto.request;

import com.crm.payroll.entity.enums.FeeType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FeeRequest {

    @NotNull
    private FeeType feeType;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String description;
}
