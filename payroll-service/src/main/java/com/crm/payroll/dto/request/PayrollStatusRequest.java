package com.crm.payroll.dto.request;

import com.crm.payroll.entity.enums.PayrollStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayrollStatusRequest {

    @NotNull
    private PayrollStatus status;
}
